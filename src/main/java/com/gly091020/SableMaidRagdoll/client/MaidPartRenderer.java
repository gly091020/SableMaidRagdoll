package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.util.GlobalDebugRenderEnable;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Objects;

public class MaidPartRenderer implements BlockEntityRenderer<MaidPartBlockEntity> {
    @Override
    public void render(@NotNull MaidPartBlockEntity blockEntity, float v, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i, int i1) {
        var renderData = blockEntity.getRenderData();
        var shape = blockEntity.getShape();
        if(renderData == null){
            drawLineBox(poseStack, multiBufferSource, blockEntity, 0, 1, 1);
            return;
        }

        if(GlobalDebugRenderEnable.enable){
            for (MaidPartBlockEntity.Box box: blockEntity.getMaidBlockShape().boxes()){
                drawLineBox(poseStack, multiBufferSource,
                        new Vec3(box.minX(), box.minY(), box.minZ()),
                        new Vec3(box.maxX(), box.maxY(), box.maxZ()),
                        1, 1, 1);
            }
            var defFile = MaidPartDefFileLoader.getDefFile(renderData.modelName());
            if(defFile == null)return;
            var vc = multiBufferSource.getBuffer(RenderType.lines());
            final double size = 0.1;
            for (MaidPartDefFileLoader.JointData jointData: defFile.jointData()){
                if(Objects.equals(jointData.partA(), renderData.partName())){
                    var pos = jointData.posA().scale(1 / 16f).add(0.5, 0.5, 0.5);
                    drawLine(poseStack, vc, pos.subtract(0, -size, 0),
                            pos.subtract(0, size, 0),
                            1, 0, 0);
                    drawLine(poseStack, vc, pos.subtract(0, 0, -size),
                            pos.subtract(0, 0, size),
                            1, 0, 0);
                    drawLine(poseStack, vc, pos.subtract(-size, 0, 0),
                            pos.subtract(size, 0, 0),
                            1, 0, 0);
                }
                if(Objects.equals(jointData.partB(), renderData.partName())){
                    var pos = jointData.posB().scale(1 / 16f).add(0.5, 0.5, 0.5);
                    drawLine(poseStack, vc, pos.subtract(0, -size, 0),
                            pos.subtract(0, size, 0),
                            0, 0, 1);
                    drawLine(poseStack, vc, pos.subtract(0, 0, -size),
                            pos.subtract(0, 0, size),
                            0, 0, 1);
                    drawLine(poseStack, vc, pos.subtract(-size, 0, 0),
                            pos.subtract(size, 0, 0),
                            0, 0, 1);
                }
            }
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

    private void drawLineBox(PoseStack poseStack, MultiBufferSource multiBufferSource, Vec3 pos1, Vec3 pos2, int r, int g, int b){
        LevelRenderer.renderLineBox(
                poseStack,
                multiBufferSource.getBuffer(RenderType.lines()),
                new AABB(pos1, pos2),
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

    private void drawLine(PoseStack poseStack, VertexConsumer vc, Vec3 pos1, Vec3 pos2, float r, float g, float b) {
        var mat = poseStack.last().pose();
        vc.addVertex(mat, (float)pos1.x, (float)pos1.y, (float)pos1.z)
                .setColor(r, g, b, 1f)
                .setNormal(0f, 1f, 0f);

        vc.addVertex(mat, (float)pos2.x, (float)pos2.y, (float)pos2.z)
                .setColor(r, g, b, 1f)
                .setNormal(0f, 1f, 0f);
    }
}
