package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
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
        if(model.isEmpty() || modelInfo.isEmpty())return;
        var texture = modelInfo.get().getTexture();

        // fixme: flatChild未实现
        var parts = new ArrayList<BedrockPart>();
        for (RagdollRenderData.EveryPart part: data.renderData().parts()){
            var p = model.get().getModelMap().get(part.partName());
            if(p != null)parts.add(p);
        }
        MaidModelHelper.resetModel(model.get());
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
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
    }
}
