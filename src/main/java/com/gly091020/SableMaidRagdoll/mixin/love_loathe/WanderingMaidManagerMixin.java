package com.gly091020.SableMaidRagdoll.mixin.love_loathe;

import com.github.JumDa5he.callresponse.compat.wandering.WanderingMaidManager;
import com.github.JumDa5he.callresponse.compat.wandering.WanderingMaidSavedData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.WanderingMaidTntCakePriority;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WanderingMaidManager.class, remap = false)
public class WanderingMaidManagerMixin {
    @Inject(method = "tickApproach", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sableMaidRagdoll$prioritizeTntCake(EntityMaid maid, ServerPlayer player,
                                                           WanderingMaidSavedData savedData, long gameTime,
                                                           CallbackInfo ci) {
        if (WanderingMaidTntCakePriority.tick(maid, gameTime)) {
            ci.cancel();
        }
    }
}
