package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.util.MixinFunction;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityBox.class)
public abstract class EntityBoxMixin {
    @Shadow
    public abstract int getOpenStage();

    @Shadow
    private int thirdStageTicks;
    @Unique
    private boolean sableMaidRagdoll$dropped = false;

    @Unique
    private Ragdoll sableMaidRagdoll$ragdoll;

    @Inject(method = "baseTick", at = @At("RETURN"))
    public void tickBox(CallbackInfo ci){
        var self = ((EntityBox)(Object)this);
        if(self.level().isClientSide)return;
        if(!sableMaidRagdoll$dropped && getOpenStage() == 2 && thirdStageTicks > 1){
            sableMaidRagdoll$dropped = true;
            var v = self.getFirstPassenger();
            if(!(v instanceof EntityMaid maid))return;
            sableMaidRagdoll$dropIt(self, maid);
        }
        if(thirdStageTicks > 50 && sableMaidRagdoll$ragdoll != null){
            sableMaidRagdoll$ragdoll.remove();
            sableMaidRagdoll$ragdoll = null;
        }
    }

    @Unique
    private void sableMaidRagdoll$dropIt(EntityBox self, EntityMaid maid){
        var reg = MixinFunction.getRagdoll(self, maid);
        if (reg == null) return;
        sableMaidRagdoll$ragdoll = reg;
    }

    @Inject(method = "kill", at = @At("RETURN"))
    private void onKill(CallbackInfo ci){
        var self = ((EntityBox)(Object)this);
        if(self.level().isClientSide)return;
        if(sableMaidRagdoll$ragdoll != null)
            sableMaidRagdoll$ragdoll.remove();
    }
}
