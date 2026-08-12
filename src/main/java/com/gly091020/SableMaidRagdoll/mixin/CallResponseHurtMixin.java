package com.gly091020.SableMaidRagdoll.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.EventHandler;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 通过 MixinSquared 注入 callresponse 的 {@code MixinEntityMaid#example$onHurtHead}。
 * <p>
 * callresponse 会把主人攻击的来源替换成中立来源（generic）并直接 super.hurt，
 * 导致 MaidHurtEvent 拿不到攻击者。当本次伤害应由本模组处理（主人攻击带物品/特殊伤害）
 * 时，直接取消该 handler，让 EntityMaid#hurt 的原版流程继续，事件里就能拿到真实来源。
 * <p>
 * require = 0：未安装 callresponse 时该 handler 不存在，本注入自动跳过。
 */
@Mixin(value = EntityMaid.class, priority = 1001)
@Pseudo
public class CallResponseHurtMixin {
    @TargetHandler(mixin = "com.github.JumDa5he.callresponse.mixin.MixinEntityMaid", name = "example$onHurtHead")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 0)
    private void sableMaidRagdoll$skipCallResponseBypass(DamageSource source, float amount,
                                                         CallbackInfoReturnable<Boolean> cir, CallbackInfo ci) {
        if (EventHandler.shouldSkipCallResponseBypass((EntityMaid) (Object) this, source)) {
            ci.cancel();
        }
    }
}
