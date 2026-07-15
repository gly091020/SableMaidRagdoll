package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockCube;
import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.gly091020.SableRagdollLib.resource.file.RagdollRenderData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MaidModelHelper {
    public static List<String> IGNORE_PART = List.of(
            "ahoge",
            "begShow",
            "blink",
            "blink2",
            "hurtBlink",
            "danmakuAttackShow"
    );

    public static void resetModel(BedrockModel<?> model){
        model.getModelMap().values().forEach(MaidModelHelper::resetModel);

        IGNORE_PART.forEach(p -> hidePart(model.getModelMap().get(p)));
    }

    public static void hidePart(BedrockPart part){
        if(part == null)return;
        part.visible = false;
    }

    public static void showPart(BedrockPart part){
        if(part == null)return;
        part.visible = true;
    }

    public static void resetModel(BedrockPart part){
        // 943写的怎么是全局共享模型的?
        // 943的代码真让人着迷
        part.xRot = part.initRotX;
        part.yRot = part.initRotY;
        part.zRot = part.initRotZ;
        part.offsetX = 0;
        part.offsetY = 0;
        part.offsetZ = 0;
    }

    public static MutableComponent paste943String(String s){
        final Pattern pattern = Pattern.compile("^\\{(.*)}$");
        var matcher = pattern.matcher(s);
        if(matcher.find())
            return Component.translatable(matcher.group(1));
        return Component.literal(s);
    }

    public static void renderBedrockParts(BedrockModel<?> model, ResourceLocation texture, List<RagdollRenderData.EveryPart> parts, PoseStack poseStack, MultiBufferSource bufferSource) {
        renderBedrockParts(model, texture, parts, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    public static void renderBedrockParts(BedrockModel<?> model, ResourceLocation texture, List<RagdollRenderData.EveryPart> parts, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay){
        var partList = new ArrayList<Pair<BedrockPart, Boolean>>();
        var map = model.getModelMap();
        for (RagdollRenderData.EveryPart everyPart: parts){
            var p = map.get(everyPart.partName());
            if(p != null)partList.add(new Pair<>(p, everyPart.flatChild()));
        }

        var vc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        for (Pair<BedrockPart, Boolean> pair: partList){
            if(pair.getSecond())
                renderBedrockPartsWithFlat(pair.getFirst(), poseStack, vc, light, overlay);
            else{
                renderBedrockPart(pair.getFirst(), poseStack, vc, light, overlay);
            }
        }
    }

    private static void transformParent(BedrockPart part, PoseStack poseStack){
        if(part.parent != null)
            transformParent(part.parent, poseStack);
        part.translateAndRotateAndScale(poseStack);
    }

    // 抄代码啊啊啊啊啊啊啊
    public static void renderBedrockPartsWithFlat(BedrockPart bedrockPart, PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay){
        if (bedrockPart.visible) {
            boolean xNearZero = -1E-5F < bedrockPart.xScale && bedrockPart.xScale < 1E-5F;
            boolean yNearZero = -1E-5F < bedrockPart.yScale && bedrockPart.yScale < 1E-5F;
            boolean zNearZero = -1E-5F < bedrockPart.zScale && bedrockPart.zScale < 1E-5F;
            if ((xNearZero && yNearZero) || (xNearZero && zNearZero) || (yNearZero && zNearZero)) {
                return;
            }

            if (!bedrockPart.cubes.isEmpty() || !bedrockPart.children.isEmpty()) {
                poseStack.pushPose();
                transformParent(bedrockPart, poseStack);
                compileBedrockPart(bedrockPart, poseStack.last(), vertexConsumer, light, overlay);
                poseStack.popPose();
            }
        }
    }

    public static void renderBedrockPart(BedrockPart bedrockPart, PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay){
        if (bedrockPart.visible) {
            boolean xNearZero = -1E-5F < bedrockPart.xScale && bedrockPart.xScale < 1E-5F;
            boolean yNearZero = -1E-5F < bedrockPart.yScale && bedrockPart.yScale < 1E-5F;
            boolean zNearZero = -1E-5F < bedrockPart.zScale && bedrockPart.zScale < 1E-5F;
            if ((xNearZero && yNearZero) || (xNearZero && zNearZero) || (yNearZero && zNearZero)) {
                return;
            }

            if (!bedrockPart.cubes.isEmpty() || !bedrockPart.children.isEmpty()) {
                poseStack.pushPose();
                transformParent(bedrockPart, poseStack);
                compileBedrockPart(bedrockPart, poseStack.last(), vertexConsumer, light, overlay);
                for (BedrockPart part : bedrockPart.children) {
                    part.render(poseStack, vertexConsumer, light, overlay, 1, 1, 1, 1);
                }
                poseStack.popPose();
            }
        }
    }

    private static final Vector3f[] NORMALS = new Vector3f[6];
    private static void compileBedrockPart(BedrockPart bedrockPart, PoseStack.Pose pose, VertexConsumer consumer, int lightmap, int overlay) {
        Matrix3f normal = pose.normal();
        NORMALS[0].set(-normal.m10, -normal.m11, -normal.m12);
        NORMALS[1].set(normal.m10, normal.m11, normal.m12);
        NORMALS[2].set(-normal.m20, -normal.m21, -normal.m22);
        NORMALS[3].set(normal.m20, normal.m21, normal.m22);
        NORMALS[4].set(-normal.m00, -normal.m01, -normal.m02);
        NORMALS[5].set(normal.m00, normal.m01, normal.m02);
        for (BedrockCube bedrockCube : bedrockPart.cubes) {
            bedrockCube.compile(pose, NORMALS, consumer, lightmap, overlay, 1, 1, 1, 1);
        }
    }
    static {
        for (int i = 0; i < NORMALS.length; i++) {
            NORMALS[i] = new Vector3f();
        }
    }
}
