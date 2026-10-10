package com.gly091020.SableMaidRagdoll.mixin.feast_wine_gone;

import com.feastwineallgone.behavior.DrunkSleepManager;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DrunkSleepManager.class)
public class DrunkSleepManagerMixin {
    @Inject(method = "startDrunkSleep", at = @At("RETURN"))
    private static void sleep(ServerLevel level, EntityMaid maid, BlockPos sleepPos, float yaw, CallbackInfo ci){
        if(maid.getVehicle() instanceof PartSeat)return;
        MaidRagdollTypesManager.createRagdoll(maid, Vec3.ZERO, new Vec3(10, 0, 0), true);
    }

    @Inject(method = "wakeWithoutReward", at = @At(value = "INVOKE", target = "Lcom/feastwineallgone/network/WgonNetwork;broadcastDrunkSleepState(Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;ZLnet/minecraft/core/BlockPos;F)V"))
    private static void wakeUp(EntityMaid maid, CallbackInfoReturnable<Boolean> cir){
        maid.stopRiding();
    }
}
