package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.gly091020.SableRagdollLib.client.renderer.AbstractPartBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Random;

public class MaidPartRenderer extends AbstractPartBlockRenderer<MaidPartBlockEntity> {
    @Override
    public void renderMain(MaidPartBlockEntity blockEntity, float delta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        var data = blockEntity.getPartData();
        if (!data.defFile().getNamespace().equals(SableMaidRagdoll.MODID)) return;

        var cache = MaidPartRenderCache.get(data.defFile());
        var modelInfo = cache.info();
        if (modelInfo == null) return;
        if (GeoMaidModelRenderer.render(blockEntity, delta, poseStack, multiBufferSource, light, overlay, modelInfo)) return;

        if (cache.model() == null) return;

        cache.reset();
        cache.applyInit(data.expressions());

        poseStack.pushPose();
        var vc = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(cache.texture()));
        var scale = cache.scale();
        poseStack.scale(scale, scale, scale);

        for (var renderPart : cache.parts(data.partName(), data.renderData().parts())) {
            if (renderPart.flatChild()) {
                MaidModelHelper.renderBedrockPartsWithFlat(renderPart.part(), poseStack, vc, light, overlay);
            } else {
                MaidModelHelper.renderBedrockPart(renderPart.part(), poseStack, vc, light, overlay);
            }
        }

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

    public static final ResourceLocation STAR = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "textures/star.png");
    public static void renderStars(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 center, float partialTick, int count, float radius, float size, int light) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(STAR));
        poseStack.pushPose();
        poseStack.translate(center.x, center.y, center.z);
        Random random = new Random(114514L);
        float time = (Minecraft.getInstance().level.getGameTime() + partialTick) * 0.15f;
        for (int i = 0; i < count; i++) {
            double angle = time + i * Math.PI * 2 / count;
            var r = radius + random.nextFloat() / 10;
            float x = (float) Math.cos(angle) * r;
            float z = (float) Math.sin(angle) * r;
            poseStack.pushPose();
            poseStack.translate(x, 0.3f + random.nextFloat() * 0.1, z);
            Vec3 starPos = center.add(x, 0.3f, z);
            double dx = center.x - starPos.x;
            double dz = center.z - starPos.z;
            float yaw = (float) Math.atan2(dx, dz);
            poseStack.mulPose(Axis.YP.rotation(yaw));
            poseStack.mulPose(Axis.ZP.rotation(random.nextFloat() * 0.5f));
            Matrix4f matrix = poseStack.last().pose();
            consumer.addVertex(matrix, -size, -size, 0).setUv(0, 0).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            consumer.addVertex(matrix, size, -size, 0).setUv(1, 0).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            consumer.addVertex(matrix, size, size, 0).setUv(1, 1).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            consumer.addVertex(matrix, -size, size, 0).setUv(0, 1).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public void firstRender(MaidPartBlockEntity blockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        if(blockEntity.getPartData().partName().contains("head") && blockEntity.getEntity() != null) {
            var height = blockEntity.getShape().max(Direction.Axis.Y) - blockEntity.getShape().min(Direction.Axis.Y);
            renderStars(poseStack, multiBufferSource, Vec3.ZERO.add(0.5, 0.5 - height / 2, 0.5), v, 10, 0.5f, 0.1f, LightTexture.FULL_BRIGHT);
        }
    }
}
