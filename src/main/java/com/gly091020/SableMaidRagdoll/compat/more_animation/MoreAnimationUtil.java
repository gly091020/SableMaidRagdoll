package com.gly091020.SableMaidRagdoll.compat.more_animation;

import com.github.JumDa5he.moreanimation.client.ActualMaid;
import com.github.JumDa5he.moreanimation.client.tears.GeckoTearAnchors;
import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.PlayState;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.AnimationBuilder;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.builder.ILoopType;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class MoreAnimationUtil {
    private MoreAnimationUtil() {
    }

    public static PlayState applyDizzinessFallback(PlayState original,
                                                   AnimationEvent<GeckoMaidEntity<?>> event) {
        if (!CompatMods.MORE_ANIMATION.isLoaded() || original != PlayState.STOP) {
            return original;
        }
        EntityMaid maid = getActualMaid(event);
        if (maid == null || !(maid.getVehicle() instanceof PartSeat)) {
            return original;
        }
        event.getController().setAnimation(new AnimationBuilder()
                .addAnimation("dizziness", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    public static void captureTears(EntityMaid maid, AnimatedGeoModel model, ResourceLocation texture,
                                    PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        if (!CompatMods.MORE_ANIMATION.isLoaded()) {
            return;
        }
        MoreAnimationHooks.captureTears(maid, model, texture, poseStack, bufferSource, light);
    }

    private static @Nullable EntityMaid getActualMaid(AnimationEvent<GeckoMaidEntity<?>> event) {
        return MoreAnimationHooks.getActualMaid(event);
    }

    private static final class MoreAnimationHooks {
        private static EntityMaid getActualMaid(AnimationEvent<GeckoMaidEntity<?>> event) {
            return ActualMaid.from(event.getAnimatableEntity());
        }

        private static void captureTears(EntityMaid maid, AnimatedGeoModel model, ResourceLocation texture,
                                         PoseStack poseStack, MultiBufferSource bufferSource, int light) {
            // fixme:由于渲染眼泪的时机在方块实体渲染，更多动画不允许这么做，实际还没用
            GeckoTearAnchors.capture(maid, model, texture, poseStack, bufferSource, light);
        }
    }
}
