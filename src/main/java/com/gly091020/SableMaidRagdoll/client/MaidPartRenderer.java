package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.gly091020.SableRagdollLib.client.renderer.AbstractPartBlockRenderer;
import com.gly091020.SableRagdollLib.resource.file.RagdollRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;

public class MaidPartRenderer extends AbstractPartBlockRenderer<MaidPartBlockEntity> {
    @Override
    public void renderMain(MaidPartBlockEntity blockEntity, float delta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        var data = blockEntity.getPartData();
        if(!data.defFile().getNamespace().equals(SableMaidRagdoll.MODID))return;
        var modelID = data.defFile().getPath().replace("/", ":");
        var model = CustomPackLoader.MAID_MODELS.getModel(modelID);
        var modelInfo = CustomPackLoader.MAID_MODELS.getInfo(modelID);

        if(modelInfo.isEmpty())return;
        if(GeoMaidModelRenderer.render(blockEntity, delta, poseStack, multiBufferSource, light, overlay, modelInfo.get())){
            return;
        }

        if(model.isEmpty())return;
        var texture = modelInfo.get().getTexture();

        // fixme: flatChild 未实现
        var parts = new ArrayList<BedrockPart>();
        for (RagdollRenderData.EveryPart part: data.renderData().parts()){
            var p = model.get().getModelMap().get(part.partName());
            if(p != null)parts.add(p);
        }
        MaidModelHelper.resetModel(model.get());

        blockEntity.getPartData().expressions().getExpression("init").ifPresent(expressionMap ->
                expressionMap.forEach((partName, expression) -> {
                    var p = model.get().getModelMap().get(partName);
                    switch (expression.actionType()){
                        case "hide": MaidModelHelper.hidePart(p);
                        case "show": MaidModelHelper.showPart(p);
                    }
                    p.offsetX = (float) expression.transform().x;
                    p.offsetY = (float) expression.transform().y;
                    p.offsetZ = (float) expression.transform().z;
                    p.xRot = (float) Math.toRadians(expression.transform().x);
                    p.yRot = (float) Math.toRadians(expression.transform().y);
                    p.zRot = (float) Math.toRadians(expression.transform().z);
                }));

        poseStack.pushPose();
        var scale = modelInfo.get().getRenderEntityScale();
        var vc = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        poseStack.scale(scale, scale, scale);

        parts.forEach(part -> part.render(poseStack, vc, light, overlay));

        poseStack.popPose();
    }

    @Override
    public void transformBefore(MaidPartBlockEntity blockEntity, PoseStack poseStack) {
        var shape = blockEntity.getShape();
        var height = shape.bounds().maxY - shape.bounds().minY;
        poseStack.translate(0.5, 1.5d + (1 - height) / 2, 0.5);
        if(GeoMaidModelRenderer.isGeo(blockEntity)){
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            return;
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
    }
}
