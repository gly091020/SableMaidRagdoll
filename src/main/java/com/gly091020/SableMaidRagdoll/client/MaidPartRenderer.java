package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import com.gly091020.SableMaidRagdoll.util.GlobalDebugRenderEnable;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
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

        if(r1.isPresent() && GeoMaidModelRenderer.render(blockEntity, v, poseStack, multiBufferSource, i, i1, r1.get()))
            return;

        if(r.isEmpty() || r1.isEmpty()){
            drawLineBox(poseStack, multiBufferSource, blockEntity, 0, 1, 0);
            return;
        }
        var model = r.get();
        var texture = r1.get().getTexture();
        var vc = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        var height = shape.bounds().maxY - shape.bounds().minY;

        var parts = new ArrayList<BedrockPart>();
        if(renderData.partName().contains("|"))
            for (String pName: renderData.partName().split("\\|")){
                var p = model.getModelMap().get(pName);
                if(p != null)parts.add(p);
            }
        else{
            var p = model.getModelMap().get(renderData.partName());
            if(p != null)parts.add(p);
        }

        // fixme: flatChild未实现
        var extraParts = new ArrayList<BedrockPart>();
        for(String pName: renderData.extraPart()){
            var p = model.getModelMap().get(pName);
            if(p != null)extraParts.add(p);
        }
        if(parts.isEmpty()){
            drawLineBox(poseStack, multiBufferSource, blockEntity, 1, 0, 0);
            return;
        }

        MaidModelHelper.resetModel(model);

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5d + (1 - height) / 2, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        poseStack.translate(renderData.transform().x, renderData.transform().y, renderData.transform().z);
        poseStack.mulPose(new Quaternionf().rotateXYZ((float) Math.toRadians(renderData.rotate().x),
                (float) Math.toRadians(renderData.rotate().y),
                (float) Math.toRadians(renderData.rotate().z)));
        var scale = r1.get().getRenderEntityScale();
        poseStack.scale(scale, scale, scale);

        parts.forEach(part -> part.render(poseStack, vc, i, i1));
        extraParts.forEach(part -> part.render(poseStack, vc, i, i1));
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
