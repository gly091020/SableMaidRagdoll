package com.gly091020.SableMaidRagdoll.compat.player_ragdoll;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import dev.leo.sableplayerragdoll.api.RagdollAPI;
import dev.leo.sableplayerragdoll.mob.api.MobRagdollLaunchOptions;
import net.minecraft.server.level.ServerPlayer;
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
        if(!serverPlayer.getMainHandItem().is(SableMaidRagdoll.MAID_TO_RAGDOLL_TAG))return;

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
            // todo:由于Sable: Ragdolls无法判断生物是否支持布娃娃化，取消音效播放
//            if(SableMaidRagdoll.CONFIG.sounds.watermelonHurt) {
//                event.getTarget().level().playSound(null, BlockPos.containing(event.getTarget().position()), SableMaidRagdoll.WATERMELON_HURT.get(), SoundSource.PLAYERS, 1, 1);
//            }
//            else if(SableMaidRagdoll.CONFIG.sounds.metalPipe) {
//                event.getTarget().level().playSound(null, BlockPos.containing(event.getTarget().position()), SableMaidRagdoll.PIPE.get(), SoundSource.PLAYERS, 1, 1);
//            }
        }
    }
}
