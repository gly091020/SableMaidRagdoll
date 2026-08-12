package com.gly091020.SableMaidRagdoll.mixin.love_loathe;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.github.JumDa5he.callresponse.compat.broadcast.actions.StandUpAction", remap = false)
@Pseudo
public class StandUpActionMixin {
    @Inject(method = "execute", at = @At(value = "HEAD"))
    private static void standUp(EntityMaid maid, ServerPlayer debugPlayer, CallbackInfo ci){
        if(maid.getVehicle() instanceof PartSeat)maid.stopRiding();
    }
}
