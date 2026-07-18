package com.gly091020.SableMaidRagdoll.editor;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.models.MaidModels;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidRenderer;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.gly091020.SableMaidRagdoll.util.MixinFunction;
import com.gly091020.SableRagdollLib.editor.api.AbstractModelSceneObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class MaidModelObject extends AbstractModelSceneObject {
    private final BedrockModel<?> model;
    private final MaidModelInfo info;

    private AnimatedGeoModel geoModel = null;

    public MaidModelObject(ResourceLocation modelID){
        var model = MaidModels.getInstance().getModel(modelID.toString());
        var info = MaidModels.getInstance().getInfo(modelID.toString());
        this.info = info.orElse(null);
        this.model = model.orElse(null);

        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(modelID);
        if (geoModel == null) return;
        this.geoModel = new AnimatedGeoModel(geoModel);
    }
    @Override
    public void drawMain(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, float alpha) {
        if(info == null)return;
        if(geoModel != null){
            geoDrawMain(poseStack, bufferSource, partialTicks, alpha);
            return;
        }
        MaidModelHelper.resetModel(model);
        poseStack.pushPose();
        poseStack.scale(info.getRenderEntityScale(), -info.getRenderEntityScale(), info.getRenderEntityScale());
        poseStack.translate(0, -1, 0);
        if(alpha == 1)
            model.renderToBuffer(poseStack,
                    bufferSource.getBuffer(RenderType.entityCutoutNoCull(info.getTexture())),
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        else {
            var vc = bufferSource.getBuffer(RenderType.entityTranslucent(info.getTexture()));
            MixinFunction.getShouldRender(model).forEach(part ->
                    part.render(poseStack, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, alpha));
        }
        poseStack.popPose();
    }

    @Override
    public void drawPart(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        if(info == null)return;
        if(geoModel != null){
            geoDrawPart(poseStack, bufferSource, partialTicks);
            return;
        }
        MaidModelHelper.resetModel(model);
        poseStack.pushPose();
        poseStack.scale(info.getRenderEntityScale(), -info.getRenderEntityScale(), info.getRenderEntityScale());
        poseStack.translate(0, -1, 0);
        MaidModelHelper.renderBedrockParts(model, info.getTexture(), parts, poseStack, bufferSource);
        poseStack.popPose();
    }

    public void geoDrawMain(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, float alpha){
        poseStack.pushPose();
        poseStack.scale(info.getRenderEntityScale(), info.getRenderEntityScale(), info.getRenderEntityScale());
        var renderer = new GeoMaidRenderer(bufferSource, info.getTexture());
        var renderType = alpha == 1 ? RenderType.entityCutoutNoCull(info.getTexture()) : RenderType.entityTranslucent(info.getTexture());
        renderer.render(geoModel, null, partialTicks, renderType, poseStack, bufferSource, bufferSource.getBuffer(renderType), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, alpha);
        poseStack.popPose();
    }

    public void geoDrawPart(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks){
        poseStack.pushPose();
        poseStack.scale(info.getRenderEntityScale(), info.getRenderEntityScale(), info.getRenderEntityScale());
        var renderer = new GeoMaidRenderer(bufferSource, info.getTexture());
        var renderType = RenderType.entityCutoutNoCull(info.getTexture());
        renderer.render(geoModel, null, partialTicks, renderType, poseStack, bufferSource, bufferSource.getBuffer(renderType), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, parts);
        poseStack.popPose();
    }
}
