package com.gly091020.SableMaidRagdoll.block.giant_slime;

import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintHandle;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GiantSlimeBlockEntity extends BlockEntity {
    private static final double ALIGNED_ANGLE = Math.toRadians(30.0);
    private static final double SOFT_BREAK_DISTANCE = 0.75;
    private static final double FIXED_BREAK_DISTANCE = 1.5;
    private static final double SOFT_LINEAR_STIFFNESS = 800;
    private static final double SOFT_LINEAR_DAMPING = 60;
    private static final double SOFT_LINEAR_MAX_FORCE = 800;
    private static final double SOFT_ANGULAR_STIFFNESS = 500;
    private static final double SOFT_ANGULAR_DAMPING = 50;
    private static final double SOFT_ANGULAR_MAX_TORQUE = 400;

    private @Nullable BlockPos masterPos;
    private final Map<UUID, PendingAdhesion> pending = new ConcurrentHashMap<>();
    private final Map<UUID, Adhesion> adhesions = new HashMap<>();

    public GiantSlimeBlockEntity(BlockPos pos, BlockState state) {
        super(InitBlockEntities.GIANT_SLIME_BLOCK_ENTITY.get(), pos, state);
    }

    public @Nullable BlockPos getMasterPos() {
        return masterPos;
    }

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos.immutable();
        setChanged();
    }

    public void queueCollision(ServerSubLevel otherSubLevel, Vector3d anchorThis, Vector3d anchorOther,
                               Vector3d normalThis, Vector3d normalOther, boolean aligned) {
        UUID targetId = otherSubLevel.getUniqueId();
        PendingAdhesion incoming = new PendingAdhesion(
                targetId,
                new Vector3d(anchorThis),
                new Vector3d(anchorOther),
                new Vector3d(normalThis),
                new Vector3d(normalOther),
                aligned
        );
        pending.compute(targetId, (id, old) ->
                old == null || (!old.aligned() && incoming.aligned()) ? incoming : old);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GiantSlimeBlockEntity blockEntity) {
        if (level.isClientSide
                || state.getValue(GiantSlimeBlock.PART) != GiantSlimePart.CORE) {
            return;
        }
        blockEntity.tickAdhesions((ServerLevel) level, pos);
    }

    private void tickAdhesions(ServerLevel level, BlockPos pos) {
        if (!(Sable.HELPER.getContaining(level, pos) instanceof ServerSubLevel thisSubLevel)) {
            clearAdhesions();
            return;
        }
        ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
        if (container == null) {
            clearAdhesions();
            return;
        }

        for (UUID targetId : Set.copyOf(pending.keySet())) {
            PendingAdhesion pendingAdhesion = pending.remove(targetId);
            if (pendingAdhesion != null) {
                applyPending(container, thisSubLevel, pendingAdhesion);
            }
        }

        Iterator<Map.Entry<UUID, Adhesion>> iterator = adhesions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Adhesion> entry = iterator.next();
            Adhesion adhesion = entry.getValue();
            ServerSubLevel otherSubLevel = container.getSubLevel(entry.getKey()) instanceof ServerSubLevel sub
                    ? sub : null;
            if (otherSubLevel == null || otherSubLevel.isRemoved()
                    || adhesion.joint() == null || !adhesion.joint().isValid()
                    || isSeparated(thisSubLevel, otherSubLevel, adhesion)) {
                removeJoint(adhesion.joint());
                iterator.remove();
                continue;
            }
            if (!adhesion.fixed()
                    && faceAngle(thisSubLevel, otherSubLevel,
                    adhesion.normalThis(), adhesion.normalOther()) <= ALIGNED_ANGLE) {
                upgradeToFixed(container, thisSubLevel, otherSubLevel, adhesion);
            }
        }
    }

    private void applyPending(ServerSubLevelContainer container, ServerSubLevel thisSubLevel,
                              PendingAdhesion pendingAdhesion) {
        Adhesion existing = adhesions.get(pendingAdhesion.targetId());
        if (existing != null && existing.joint() != null && existing.joint().isValid()) {
            if (!existing.fixed() && pendingAdhesion.aligned()) {
                ServerSubLevel other = container.getSubLevel(pendingAdhesion.targetId()) instanceof ServerSubLevel sub
                        ? sub : null;
                if (other != null && !other.isRemoved()) {
                    upgradeToFixed(container, thisSubLevel, other, existing);
                }
            }
            return;
        }
        if (existing != null) {
            removeJoint(existing.joint());
            adhesions.remove(pendingAdhesion.targetId());
        }

        ServerSubLevel other = container.getSubLevel(pendingAdhesion.targetId()) instanceof ServerSubLevel sub
                ? sub : null;
        if (other == null || other.isRemoved()) {
            return;
        }
        Adhesion adhesion = new Adhesion(
                pendingAdhesion.targetId(),
                null,
                pendingAdhesion.aligned(),
                pendingAdhesion.anchorThis(),
                pendingAdhesion.anchorOther(),
                pendingAdhesion.normalThis(),
                pendingAdhesion.normalOther()
        );
        PhysicsConstraintHandle joint = pendingAdhesion.aligned()
                ? createFixedJoint(container, thisSubLevel, other, adhesion)
                : createSoftJoint(container, thisSubLevel, other, adhesion);
        if (joint != null && joint.isValid()) {
            adhesions.put(pendingAdhesion.targetId(), adhesion.withJoint(joint));
        }
    }

    private void upgradeToFixed(ServerSubLevelContainer container, ServerSubLevel thisSubLevel,
                                ServerSubLevel otherSubLevel, Adhesion adhesion) {
        PhysicsConstraintHandle fixed = createFixedJoint(container, thisSubLevel, otherSubLevel, adhesion);
        if (fixed != null && fixed.isValid()) {
            removeJoint(adhesion.joint());
            adhesion.joint = fixed;
            adhesion.fixed = true;
        }
    }

    private static @Nullable PhysicsConstraintHandle createFixedJoint(
            ServerSubLevelContainer container, ServerSubLevel thisSubLevel,
            ServerSubLevel otherSubLevel, Adhesion adhesion) {
        if (!anchorsInPlots(thisSubLevel, otherSubLevel, adhesion)) {
            return null;
        }
        Quaterniond orientation = fixedOrientation(thisSubLevel, otherSubLevel,
                adhesion.normalThis(), adhesion.normalOther());
        FixedConstraintHandle joint = container.physicsSystem().getPipeline().addConstraint(
                thisSubLevel, otherSubLevel,
                new FixedConstraintConfiguration(adhesion.anchorThis(), adhesion.anchorOther(), orientation));
        if (joint == null || !joint.isValid()) {
            return null;
        }
        playAttachFeedback(container.getLevel(), thisSubLevel, adhesion.anchorThis());
        return joint;
    }

    private static @Nullable PhysicsConstraintHandle createSoftJoint(
            ServerSubLevelContainer container, ServerSubLevel thisSubLevel,
            ServerSubLevel otherSubLevel, Adhesion adhesion) {
        if (!anchorsInPlots(thisSubLevel, otherSubLevel, adhesion)) {
            return null;
        }
        Quaterniond frameThis = new Quaterniond().rotationTo(
                adhesion.normalThis(), new Vector3d(1, 0, 0));
        Quaterniond frameOther = new Quaterniond().rotationTo(
                new Vector3d(adhesion.normalOther()).negate(), new Vector3d(1, 0, 0));
        GenericConstraintHandle joint = container.physicsSystem().getPipeline().addConstraint(
                thisSubLevel, otherSubLevel,
                new GenericConstraintConfiguration(
                        adhesion.anchorThis(), adhesion.anchorOther(),
                        frameThis, frameOther, Set.of()));
        if (joint == null || !joint.isValid()) {
            return null;
        }
        double mass = Math.max(massOf(thisSubLevel), massOf(otherSubLevel));
        joint.setMotor(ConstraintJointAxis.LINEAR_X, 0, SOFT_LINEAR_STIFFNESS * mass,
                SOFT_LINEAR_DAMPING * mass, true, SOFT_LINEAR_MAX_FORCE * mass);
        joint.setMotor(ConstraintJointAxis.LINEAR_Y, 0, SOFT_LINEAR_STIFFNESS * mass,
                SOFT_LINEAR_DAMPING * mass, true, SOFT_LINEAR_MAX_FORCE * mass);
        joint.setMotor(ConstraintJointAxis.LINEAR_Z, 0, SOFT_LINEAR_STIFFNESS * mass,
                SOFT_LINEAR_DAMPING * mass, true, SOFT_LINEAR_MAX_FORCE * mass);
        joint.setMotor(ConstraintJointAxis.ANGULAR_X, 0, SOFT_ANGULAR_STIFFNESS * mass,
                SOFT_ANGULAR_DAMPING * mass, true, SOFT_ANGULAR_MAX_TORQUE * mass);
        joint.setMotor(ConstraintJointAxis.ANGULAR_Y, 0, SOFT_ANGULAR_STIFFNESS * mass,
                SOFT_ANGULAR_DAMPING * mass, true, SOFT_ANGULAR_MAX_TORQUE * mass);
        joint.setMotor(ConstraintJointAxis.ANGULAR_Z, 0, SOFT_ANGULAR_STIFFNESS * mass,
                SOFT_ANGULAR_DAMPING * mass, true, SOFT_ANGULAR_MAX_TORQUE * mass);
        playAttachFeedback(container.getLevel(), thisSubLevel, adhesion.anchorThis());
        return joint;
    }

    private static void playAttachFeedback(ServerLevel level, ServerSubLevel subLevel, Vector3d anchor) {
        Vector3d worldPos = subLevel.logicalPose().transformPosition(
                new Vector3d(anchor), new Vector3d());
        level.sendParticles(ParticleTypes.ITEM_SLIME,
                worldPos.x, worldPos.y, worldPos.z,
                8, 0.15, 0.15, 0.15, 0.02);
        level.playSound(null, worldPos.x, worldPos.y, worldPos.z,
                SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS,
                0.8F, 0.85F + level.random.nextFloat() * 0.3F);
    }

    private static boolean anchorsInPlots(ServerSubLevel thisSubLevel, ServerSubLevel otherSubLevel,
                                          Adhesion adhesion) {
        return thisSubLevel.getPlot().contains(adhesion.anchorThis())
                && otherSubLevel.getPlot().contains(adhesion.anchorOther());
    }

    private static Quaterniond fixedOrientation(ServerSubLevel thisSubLevel, ServerSubLevel otherSubLevel,
                                                Vector3d normalThis, Vector3d normalOther) {
        Quaterniond first = thisSubLevel.logicalPose().orientation();
        Quaterniond second = otherSubLevel.logicalPose().orientation();
        Vector3d globalThis = first.transform(new Vector3d(normalThis), new Vector3d());
        Vector3d globalOther = second.transform(new Vector3d(normalOther).negate(), new Vector3d());
        double dot = Math.max(-1.0, Math.min(1.0, globalThis.dot(globalOther)));
        double angle = Math.acos(dot);
        Vector3d axis = globalThis.cross(globalOther, new Vector3d());
        Quaterniond alignment = new Quaterniond();
        if (axis.lengthSquared() > 1.0E-8) {
            axis.normalize();
            alignment.rotateAxis(-angle, axis);
        }
        return alignment.mul(second).premul(first.conjugate(new Quaterniond()));
    }

    private static double faceAngle(ServerSubLevel thisSubLevel, ServerSubLevel otherSubLevel,
                                    Vector3d normalThis, Vector3d normalOther) {
        Vector3d globalThis = thisSubLevel.logicalPose().orientation().transform(
                new Vector3d(normalThis), new Vector3d());
        Vector3d globalOther = otherSubLevel.logicalPose().orientation().transform(
                new Vector3d(normalOther), new Vector3d());
        double dot = Math.max(-1.0, Math.min(1.0, -globalThis.dot(globalOther)));
        return Math.acos(dot);
    }

    private static boolean isSeparated(ServerSubLevel thisSubLevel, ServerSubLevel otherSubLevel,
                                       Adhesion adhesion) {
        Vector3d thisAnchor = thisSubLevel.logicalPose().transformPosition(
                new Vector3d(adhesion.anchorThis()), new Vector3d());
        Vector3d otherAnchor = otherSubLevel.logicalPose().transformPosition(
                new Vector3d(adhesion.anchorOther()), new Vector3d());
        double maxDistance = adhesion.fixed() ? FIXED_BREAK_DISTANCE : SOFT_BREAK_DISTANCE;
        return thisAnchor.distanceSquared(otherAnchor) > maxDistance * maxDistance;
    }

    private static double massOf(ServerSubLevel subLevel) {
        var tracker = subLevel.getMassTracker();
        return tracker != null && tracker.getMass() > 0.001 ? tracker.getMass() : 1.0;
    }

    public void clearAdhesions() {
        pending.clear();
        for (Adhesion adhesion : adhesions.values()) {
            removeJoint(adhesion.joint());
        }
        adhesions.clear();
    }

    @Override
    public void setRemoved() {
        clearAdhesions();
        super.setRemoved();
    }

    private static void removeJoint(@Nullable PhysicsConstraintHandle joint) {
        if (joint != null && joint.isValid()) {
            joint.remove();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (masterPos != null) {
            tag.put("Master", NbtUtils.writeBlockPos(masterPos));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        masterPos = tag.contains("Master") ? NbtUtils.readBlockPos(tag, "Master").orElse(null) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private record PendingAdhesion(UUID targetId, Vector3d anchorThis, Vector3d anchorOther,
                                   Vector3d normalThis, Vector3d normalOther, boolean aligned) {
    }

    private static final class Adhesion {
        private final UUID targetId;
        private PhysicsConstraintHandle joint;
        private boolean fixed;
        private final Vector3d anchorThis;
        private final Vector3d anchorOther;
        private final Vector3d normalThis;
        private final Vector3d normalOther;

        private Adhesion(UUID targetId, @Nullable PhysicsConstraintHandle joint, boolean fixed,
                         Vector3d anchorThis, Vector3d anchorOther,
                         Vector3d normalThis, Vector3d normalOther) {
            this.targetId = targetId;
            this.joint = joint;
            this.fixed = fixed;
            this.anchorThis = new Vector3d(anchorThis);
            this.anchorOther = new Vector3d(anchorOther);
            this.normalThis = new Vector3d(normalThis);
            this.normalOther = new Vector3d(normalOther);
        }

        private UUID targetId() {
            return targetId;
        }

        private PhysicsConstraintHandle joint() {
            return joint;
        }

        private boolean fixed() {
            return fixed;
        }

        private Vector3d anchorThis() {
            return anchorThis;
        }

        private Vector3d anchorOther() {
            return anchorOther;
        }

        private Vector3d normalThis() {
            return normalThis;
        }

        private Vector3d normalOther() {
            return normalOther;
        }

        private Adhesion withJoint(PhysicsConstraintHandle joint) {
            return new Adhesion(targetId, joint, fixed, anchorThis, anchorOther, normalThis, normalOther);
        }
    }
}
