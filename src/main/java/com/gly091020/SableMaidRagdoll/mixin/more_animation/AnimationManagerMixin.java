package com.gly091020.SableMaidRagdoll.mixin.more_animation;

import com.github.tartaricacid.touhoulittlemaid.client.animation.gecko.AnimationManager;
import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.PlayState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableMaidRagdoll.compat.more_animation.MoreAnimationUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AnimationManager.class, priority = 1001)
public class AnimationManagerMixin {
    @ModifyReturnValue(method = "predicateMisc", at = @At("RETURN"), remap = false)
    private PlayState addDizzinessAnim(PlayState original, AnimationEvent<GeckoMaidEntity<?>> event) {
        if (!CompatMods.MORE_ANIMATION.isLoaded()) {
            return original;
        }
        return MoreAnimationUtil.applyDizzinessFallback(original, event);
    }
}
