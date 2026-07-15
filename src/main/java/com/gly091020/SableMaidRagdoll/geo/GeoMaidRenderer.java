package com.gly091020.SableMaidRagdoll.geo;

import com.github.tartaricacid.touhoulittlemaid.compat.embeddium.EmbeddiumCompat;
import com.github.tartaricacid.touhoulittlemaid.compat.sodium.SodiumCompat;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoRenderer;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.EModelRenderCycle;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.RenderUtils;
import com.gly091020.SableRagdollLib.resource.file.RagdollRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record GeoMaidRenderer(MultiBufferSource multiBufferSource, ResourceLocation resourceLocation) implements IGeoRenderer<Void> {
    @Override
    public MultiBufferSource getCurrentRTB() {
        return multiBufferSource;
    }

    @Override
    public ResourceLocation getTextureLocation(Void animatable) {
        return resourceLocation;
    }

    @Override
    public void renderCubesOfBone(AnimatedGeoBone bone, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        IGeoRenderer.super.renderCubesOfBone(bone, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void render(AnimatedGeoModel model, Void animatable, float partialTick, RenderType type, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        IGeoRenderer.super.render(model, animatable, partialTick, type, poseStack, bufferSource, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void render(AnimatedGeoModel model, Void animatable, float partialTick, RenderType type, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha, List<RagdollRenderData.EveryPart> renderParts) {
        setCurrentRTB(bufferSource);
        renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight,
                packedOverlay, red, green, blue, alpha);
        if (bufferSource != null) {
            buffer = bufferSource.getBuffer(type);
        }
        renderLate(animatable, poseStack, partialTick, bufferSource, buffer, packedLight,
                packedOverlay, red, green, blue, alpha);

        for (RagdollRenderData.EveryPart renderPart: renderParts){
            var p = model.bones().get(renderPart.partName());
            if(p == null)continue;
            if(renderPart.flatChild())
                renderRecursivelyWithoutChild(p, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                        alpha);
            else
                renderRecursively(p, poseStack, buffer, packedLight, packedOverlay, red, green, blue,
                        alpha);
        }

        setCurrentModelRenderCycle(EModelRenderCycle.REPEATED);
    }

    private void renderRecursivelyWithoutChild(AnimatedGeoBone bone, PoseStack poseStack, VertexConsumer buffer, int packedLight,
                                   int packedOverlay, float red, float green, float blue, float alpha) {
        int cubePackedLight = bone.geoBone().glow() ? LightTexture.pack(15, 15) : packedLight;
        if ((bone.getScaleX() == 0 ? 0 : 1) + (bone.getScaleY() == 0 ? 0 : 1) + (bone.getScaleZ() == 0 ? 0 : 1) < 2) {
            return;
        }
        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);
        if (!SodiumCompat.sodiumRenderCubesOfBone(bone, poseStack, buffer, cubePackedLight, packedOverlay, red, green, blue, alpha)
                && !EmbeddiumCompat.embeddiumRenderCubesOfBone(bone, poseStack, buffer, cubePackedLight, packedOverlay, red, green, blue, alpha)
        ) {
            renderCubesOfBone(bone, poseStack, buffer, cubePackedLight, packedOverlay, red, green, blue, alpha);
        }
        poseStack.popPose();
    }
}
