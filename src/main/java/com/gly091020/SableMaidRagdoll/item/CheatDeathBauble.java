package com.gly091020.SableMaidRagdoll.item;

import com.github.tartaricacid.touhoulittlemaid.api.bauble.IMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.RagdollManager;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Vector3d;

import static com.gly091020.SableRagdollLib.api.ScheduleManager.scheduleDelayed;

public class CheatDeathBauble implements IMaidBauble {
    public static final int MIN_HEALTH = 5;
    public static final int RECOVER_HEALTH = 20;
    @Override
    public boolean onInjured(EntityMaid maid, ItemStack baubleItem, DamageSource source, MutableFloat damage) {
        if(isCheatDeath(maid))return true;
        if(maid.getHealth() < MIN_HEALTH){
            if(maid.level() instanceof ServerLevel serverLevel && toRagdoll(maid, damage.floatValue())){
                baubleItem.hurtAndBreak(1, serverLevel, maid, item -> {});
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDeath(EntityMaid maid, ItemStack baubleItem, DamageSource source) {
        if(isCheatDeath(maid))return true;
        maid.setHealth(1);
        if(maid.level() instanceof ServerLevel serverLevel && toRagdoll(maid, 100)){
            baubleItem.hurtAndBreak(1, serverLevel, maid, item -> {});
            return true;
        }
        return false;
    }

    @Override
    public void onTick(EntityMaid maid, ItemStack baubleItem) {
        if(!isCheatDeath(maid))return;
        if(maid.tickCount % 20 != 0)return;
        if(!(maid.level() instanceof ServerLevel serverLevel))return;
        maid.setHealth(maid.getHealth() + 2);
        spawnParticles(serverLevel, maid);

        if(maid.getHealth() >= RECOVER_HEALTH && maid.getVehicle() instanceof PartSeat partSeat){
            var container = SubLevelContainer.getContainer(serverLevel);
            if(container == null)return;
            var subLevel = container.getSubLevel(partSeat.getMainUUID());
            if(subLevel == null)return;
            if(!(subLevel.getPlot().getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO) instanceof AbstractPartBlockEntity partBlockEntity))return;
            var rag = RagdollManager.get(partBlockEntity.getPartData().ragdollUUID());
            if(rag == null)return;
            rag.remove();
        }
    }

    public static void spawnParticles(ServerLevel serverLevel, EntityMaid maid){
        float width = maid.getBbWidth();
        float height = maid.getBbHeight();
        serverLevel.sendParticles(
                ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.9f, 0.1f, 0.1f),
                maid.getX(),
                maid.getY() + height / 2,
                maid.getZ(),
                30,
                width, height / 2, width,
                0.0
        );
    }

    public static boolean isCheatDeath(EntityMaid maid){
        return maid.getVehicle() instanceof PartSeat;
    }

    public static boolean toRagdoll(EntityMaid maid, float damage){
        if(maid.level().isClientSide)return false;
        var level = (ServerLevel) maid.level();
        var container = SubLevelContainer.getContainer(level);
        if(container == null)return false;
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/"));
        var system = container.physicsSystem();
        var parts = RagdollHelper.createRagdoll(level, maid.position().add(0, 0.5, 0),
                id);
        if(parts == null)return false;
        var maidMotion = JOMLConversion.toJOML(maid.getDeltaMovement()).mul(3);

        maidMotion.mul(Math.clamp(damage / 5, 0.3, 1.5));
        parts.addEntity(maid);

        // 等待 2tick 是为了等待刚体创建在施加推力
        scheduleDelayed(level, 2, () -> {
            parts.getSublevels().forEach(subLevel -> {
                if (subLevel.isRemoved()) return;

                var f = subLevel.logicalPose().transformNormalInverse(new Vector3d(maidMotion));
                system.getPhysicsHandle(subLevel).applyLinearImpulse(f.add(0, 3, 0));
            });

            Vector3d velocity = new Vector3d(maidMotion);
            double speed = velocity.length();
            Vector3d angularAxis;
            if (speed > 1.0e-4) {
                angularAxis = new Vector3d(velocity).normalize().cross(new Vector3d(0, 1, 0));
                if (angularAxis.lengthSquared() < 1.0e-6) {
                    angularAxis = new Vector3d(1, 0, 0);
                } else {
                    angularAxis.normalize();
                }
            } else {
                angularAxis = new Vector3d(1, 0, 0);
            }
            double angularStrength = speed * 0.5;
            double jitter = (level.random.nextDouble() - 0.5) * 0.2;
            angularAxis.mul(angularStrength + jitter);
            parts.addAngularImpulse(angularAxis.mul(-3, 1, 1), true);
        });

        if (maid.getLastAttacker() instanceof Mob mob) {
            mob.setTarget(null);
        }

        return true;
    }
}
