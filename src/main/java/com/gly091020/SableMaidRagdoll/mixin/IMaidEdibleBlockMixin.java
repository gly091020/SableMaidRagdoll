package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.block.IMaidEdibleBlock;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.util.MixinFunction;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IMaidEdibleBlock.class)
public interface IMaidEdibleBlockMixin {
    @Inject(method = "belowIsSnackStand", at = @At("HEAD"), cancellable = true)
    private static void alwaysEat(EntityMaid maid, BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        if(MixinFunction.alwaysCanEat)cir.setReturnValue(true);
    }
}
