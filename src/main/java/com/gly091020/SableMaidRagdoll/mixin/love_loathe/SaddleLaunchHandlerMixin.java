package com.gly091020.SableMaidRagdoll.mixin.love_loathe;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MixinFunction;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.github.tartaricacid.callresponse.compat.emotion.SaddleLaunchHandler")
@Pseudo
public class SaddleLaunchHandlerMixin {
    @Inject(method = "onRightClickItem", at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;setDeltaMovement(DDD)V"))
    public void ragdoll(PlayerInteractEvent.RightClickItem event, CallbackInfo ci, @Local(name = "maid")EntityMaid maid, @Local(name = "player")Player player){
        var level = event.getLevel();
        if(level.isClientSide)return;
        var look = player.getLookAngle();
        var m = new Vector3d(look.x * 2.0F, look.y * 2.0F + 0.8, look.z * 2.0F).mul(5);
        MixinFunction.createRagdoll((ServerLevel) level, maid, m, true);
        if(SableMaidRagdoll.CONFIG.sounds.drop)
            player.level().playSound(null, BlockPos.containing(player.position()), SableMaidRagdoll.DROP.get(), SoundSource.PLAYERS, 1, 1);
    }
}
