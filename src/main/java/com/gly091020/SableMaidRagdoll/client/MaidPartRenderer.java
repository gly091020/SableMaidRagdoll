package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.ArrayList;

public class MaidPartRenderer implements BlockEntityRenderer<MaidPartBlockEntity> {
    @Override
    public void render(@NotNull MaidPartBlockEntity blockEntity, float v, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i, int i1) {
        var renderData = blockEntity.getRenderData();
        var shape = blockEntity.getShape();
        if(renderData == null){
            drawLineBox(poseStack, multiBufferSource, blockEntity, 0, 1, 1);
            return;
        }
        var r = CustomPackLoader.MAID_MODELS.getModel(renderData.modelName());
        var r1 = CustomPackLoader.MAID_MODELS.getInfo(renderData.modelName());
        if(r.isEmpty() || r1.isEmpty()){
            drawLineBox(poseStack, multiBufferSource, blockEntity, 0, 1, 0);
            return;
        }
        var model = r.get();
        var texture = r1.get().getTexture();
        var vc = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        var parts = new ArrayList<BedrockPart>();
        var height = shape.bounds().maxY - shape.bounds().minY;
        if(renderData.partName().contains("|"))
            for (String pName: renderData.partName().split("\\|")){
                var p = model.getModelMap().get(pName);
                if(p != null)parts.add(p);
            }
        else{
            var p = model.getModelMap().get(renderData.partName());
            if(p != null)parts.add(p);
        }
        if(parts.isEmpty()){
            drawLineBox(poseStack, multiBufferSource, blockEntity, 1, 0, 0);
            return;
        }
        resetModel(model);

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5d + (1 - height) / 2, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        poseStack.translate(renderData.transform().x, renderData.transform().y, renderData.transform().z);
        poseStack.mulPose(new Quaternionf().rotateXYZ((float) Math.toRadians(renderData.rotate().x),
                (float) Math.toRadians(renderData.rotate().y),
                (float) Math.toRadians(renderData.rotate().z)));

        parts.forEach(part -> part.render(poseStack, vc, i, i1));
        poseStack.popPose();
    }

    private void drawLineBox(PoseStack poseStack, MultiBufferSource multiBufferSource, MaidPartBlockEntity blockEntity, int r, int g, int b){
        LevelRenderer.renderLineBox(
                poseStack,
                multiBufferSource.getBuffer(RenderType.lines()),
                blockEntity.getShape().bounds(),
                r, g, b, 1
        );
    }

    private void resetModel(BedrockModel<?> model){
        model.getModelMap().values().forEach(this::resetModel);

        hidePart(model.getModelMap().get("ahoge"));
        hidePart(model.getModelMap().get("begShow"));
        showPart(model.getModelMap().get("blink"));
        hidePart(model.getModelMap().get("blink2"));
    }

    private void hidePart(BedrockPart part){
        if(part == null)return;
        part.visible = false;
    }

    private void showPart(BedrockPart part){
        if(part == null)return;
        part.visible = true;
    }

    private void resetModel(BedrockPart part){
        // 943写的怎么是全局共享模型的?
        // 943的代码真让人着迷
        part.xRot = part.initRotX;
        part.yRot = part.initRotY;
        part.zRot = part.initRotZ;
    }
}
