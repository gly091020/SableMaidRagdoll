package com.gly091020.SableMaidRagdoll.item;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.client.api.ITooltipAbleItem;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableMaidRagdoll.compat.player_ragdoll.PlayerRagdollUtil;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableMaidRagdoll.util.AuthorUtil;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.List;

public class SonicWaveItem extends Item implements ITooltipAbleItem {
    private static final double RANGE = 8.0;
    private static final double HALF_ANGLE_COS = 0.5;
    private static final double PUSH_STRENGTH = 3.0;
    private static final double STRUCTURE_PUSH_STRENGTH = 30;
    private static final double MAID_RAGDOLL_PUSH_STRENGTH = 8.0;
    private static final double PLAYER_RAGDOLL_PUSH_STRENGTH = 8.0;
    private static final double ANGULAR_PUSH_STRENGTH = 5.0;
    private static final double MIN_UPWARD_PUSH = 0.1;
    private static final double ENTITY_UPWARD_PUSH = 0.35;
    private static final double RAGDOLL_UPWARD_PUSH = 2.0;
    private static final double STRUCTURE_UPWARD_PUSH = 0.35;

    public SonicWaveItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if(level.isClientSide)return InteractionResultHolder.success(stack);
        if(!(level instanceof ServerLevel serverLevel))return InteractionResultHolder.success(stack);

        var origin = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) player.position());
        var look = getWorldLook(player);
        pushPhysicalStructures(serverLevel, origin, look);
        pushEntities(serverLevel, player, origin, look);

        if(SableMaidRagdoll.CONFIG.sounds.GCJCry)
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    InitSounds.GCJ_SOUND.get(), SoundSource.PLAYERS, 1, 1);
        else if (SableMaidRagdoll.CONFIG.sounds.bigDog)
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    InitSounds.DOG_CALL.get(), SoundSource.PLAYERS, 1, 1);
        else
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1, 1);

        for(int i = 1; i <= 6; i++){
            var pos = origin.add(look.scale(i * 1.5));
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }

        if(!player.isCreative())
            player.getCooldowns().addCooldown(stack.getItem(), 40);
        return InteractionResultHolder.success(stack);
    }

    private static void pushPhysicalStructures(ServerLevel level, Vec3 origin, Vec3 look){
        var container = ServerSubLevelContainer.getContainer(level);
        if(container == null)return;
        var originVector = JOMLConversion.toJOML(origin);
        for(var subLevel : container.getAllSubLevels()){
            if(!(subLevel instanceof ServerSubLevel serverSubLevel) || serverSubLevel.isRemoved())continue;
            var bounds = serverSubLevel.boundingBox();
            var center = bounds.center();
            var toCenter = new Vector3d(center).sub(originVector);
            double centerDistance = toCenter.length();
            if(centerDistance < 0.01)continue;
            var dir = toCenter.normalize();
            if(look.dot(new Vec3(dir.x, dir.y, dir.z)) < HALF_ANGLE_COS)continue;

            double nearestX = Math.max(bounds.minX(), Math.min(origin.x, bounds.maxX()));
            double nearestY = Math.max(bounds.minY(), Math.min(origin.y, bounds.maxY()));
            double nearestZ = Math.max(bounds.minZ(), Math.min(origin.z, bounds.maxZ()));
            double dx = nearestX - origin.x;
            double dy = nearestY - origin.y;
            double dz = nearestZ - origin.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if(distance > RANGE)continue;

            var massData = serverSubLevel.getMassTracker();
            if(massData == null || massData.isInvalid())continue;
            double falloff = falloff(distance);
            double mass = massData.getMass();
            var worldImpulse = new Vector3d(look.x, look.y, look.z)
                    .mul(STRUCTURE_PUSH_STRENGTH * falloff * mass);
            worldImpulse.y += structureUpwardImpulse(falloff, mass);
            var localImpulse = serverSubLevel.logicalPose().transformNormalInverse(worldImpulse);
            try {
                container.physicsSystem().getPhysicsHandle(serverSubLevel).applyLinearImpulse(localImpulse);
            } catch (Exception ignored) {
            }
        }
    }

    private static void pushEntities(ServerLevel level, Player player, Vec3 origin, Vec3 look){
        var box = player.getBoundingBox().move(origin.subtract(player.position()))
                .inflate(RANGE).expandTowards(look.scale(RANGE));
        for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)){
            if(entity == player || !entity.isAlive())continue;
            if(entity.getVehicle() instanceof PartSeat)continue;
            var target = SableCompanion.INSTANCE.projectOutOfSubLevel(
                    level, (Position) entity.getBoundingBox().getCenter());
            var to = target.subtract(origin);
            double dist = to.length();
            if(dist > RANGE || dist < 0.01)continue;
            var dir = to.normalize();
            if(look.dot(dir) < HALF_ANGLE_COS)continue;
            double falloff = falloff(dist);
            var horizontalDir = horizontalDirection(dir);
            var maidForce = ragdollLaunchForce(horizontalDir, falloff, MAID_RAGDOLL_PUSH_STRENGTH);
            if(blowMaidAway(level, entity, maidForce, horizontalDir, falloff))continue;

            entity.knockback(PUSH_STRENGTH * falloff, -horizontalDir.x, -horizontalDir.z);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, upwardPush(ENTITY_UPWARD_PUSH, falloff), 0));
            entity.hurtMarked = true;

            if(entity instanceof Player && entity.getUUID().equals(AuthorUtil.GLY))
                entity.hurt(level.damageSources().playerAttack(player), 1);

            if(CompatMods.PLAYER_RAGDOLL.isLoaded()){
                var force = ragdollLaunchForce(horizontalDir, falloff, PLAYER_RAGDOLL_PUSH_STRENGTH);
                if(entity instanceof ServerPlayer targetPlayer && !targetPlayer.isCreative())
                    PlayerRagdollUtil.launch(targetPlayer, force);
                else if(PlayerRagdollUtil.isSupport(entity))
                    PlayerRagdollUtil.launchMob(entity, force);
            }
        }
    }

    private static boolean blowMaidAway(ServerLevel level, Entity entity, Vec3 force, Vec3 dir, double falloff){
        var ragdoll = MaidRagdollTypesManager.createRagdoll(entity, entity.position().add(0, 1, 0), Vec3.ZERO,
                force, new Vec3(dir.z, ANGULAR_PUSH_STRENGTH * falloff, -dir.x), true);
        if(ragdoll == null)return false;
        return true;
    }

    private static double falloff(double distance) {
        return Math.max(0, 1 - distance / RANGE);
    }

    private static double upwardPush(double strength, double falloff) {
        return strength * falloff + MIN_UPWARD_PUSH;
    }

    private static double structureUpwardImpulse(double falloff, double mass) {
        return upwardPush(STRUCTURE_UPWARD_PUSH, falloff) * mass;
    }

    private static Vec3 ragdollLaunchForce(Vec3 horizontalDir, double falloff, double strength) {
        return horizontalDir.scale(strength * falloff)
                .add(0, upwardPush(RAGDOLL_UPWARD_PUSH, falloff), 0);
    }

    private static Vec3 horizontalDirection(Vec3 direction) {
        var horizontal = new Vec3(direction.x, 0, direction.z);
        return horizontal.lengthSqr() < 1.0E-6 ? Vec3.ZERO : horizontal.normalize();
    }

    private static Vec3 getWorldLook(Player player) {
        Vec3 look = player.getLookAngle();
        var subLevel = SableCompanion.INSTANCE.getTrackingOrVehicleSubLevel(player);
        if (subLevel == null) {
            subLevel = SableCompanion.INSTANCE.getContaining(player.level(), player.position());
        }
        return subLevel == null ? look : subLevel.logicalPose().transformNormal(look);
    }

    @Override
    public void appendHelpText(List<Component> tooltips) {
        generateComponentList(tooltips, this, 1, true);
    }

    @Override
    public void appendBackgroundText(List<Component> tooltips) {
        generateComponentList(tooltips, this, 2, false);
    }
}
