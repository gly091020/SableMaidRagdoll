package com.gly091020.SableMaidRagdoll.compat.player_ragdoll;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.init.InitTags;
import dev.leo.sableplayerragdoll.api.RagdollAPI;
import dev.leo.sableplayerragdoll.mob.api.MobRagdollLaunchOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class PlayerRagdollEventHandler {
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event){
        if(!(event.getEntity() instanceof ServerPlayer serverPlayer))return;
        if(!SableMaidRagdoll.CONFIG.playerRagdoll.attackToRagDoll)return;
        if(!serverPlayer.getMainHandItem().is(InitTags.MAID_TO_RAGDOLL_TAG))return;
        if(!PlayerRagdollUtil.isSupport(event.getTarget()))return;

        if(event.getTarget() instanceof LivingEntity livingEntity && !(event.getTarget() instanceof Player)){
            if(RagdollAPI.isMobRagdolled(livingEntity)){
                RagdollAPI.releaseMob(livingEntity);
                event.setCanceled(true);
                return;
            }
            var position = event.getEntity().position();
            var direction = livingEntity
                    .position()
                    .subtract(position)
                    .normalize();
            var motion = direction
                    .scale(5)
                    .add(0, 1, 0);
            var forward = event.getTarget().getLookAngle();
            var axis = forward.cross(new Vec3(0,-5,0));
            RagdollAPI.launchMob(serverPlayer.serverLevel(), livingEntity, motion, axis, new MobRagdollLaunchOptions(Integer.MAX_VALUE));

            var key = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType());
            if(key.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)){
                if(SableMaidRagdoll.CONFIG.sounds.watermelonHurt) {
                    event.getTarget().level().playSound(null, BlockPos.containing(event.getTarget().position()), InitSounds.WATERMELON_HURT.get(), SoundSource.PLAYERS, 1, 1);
                }
                else if(SableMaidRagdoll.CONFIG.sounds.metalPipe) {
                    event.getTarget().level().playSound(null, BlockPos.containing(event.getTarget().position()), InitSounds.PIPE.get(), SoundSource.PLAYERS, 1, 1);
                }
            }
        }
    }
}
