package com.gly091020.SableMaidRagdoll.geo;

import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.RenderUtils;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoMaidModelRenderer {
    private static final Map<ResourceLocation, GeoModel> MODELS = new HashMap<>();
    private static final Map<ResourceLocation, GeoMaidRenderer> RENDERERS = new HashMap<>();
    /** 模型路径 -> 骨骼名 -> 手持物品渲染信息 */
    private static final Map<ResourceLocation, Map<String, GeoHandRender>> HAND_CACHE = new HashMap<>();

    public static void clear() {
        MODELS.clear();
        RENDERERS.clear();
        HAND_CACHE.clear();
    }

    public static boolean isGeo(MaidPartBlockEntity blockEntity){
        var path = ResourceLocation.tryParse(blockEntity.getPartData().defFile().getPath().replace("/", ":"));
        if(path == null)return false;
        return GeckoLibCache.getInstance().getGeoModels().get(path) != null;
    }

    public static boolean render(@NotNull MaidPartBlockEntity blockEntity,
                                 float partialTick,
                                 @NotNull PoseStack poseStack,
                                 @NotNull MultiBufferSource bufferSource,
                                 int light,
                                 int overlay,
                                 MaidModelInfo maidModelInfo,
                                 ItemInHandRenderer itemInHandRenderer) {

        var renderData = blockEntity.getPartData().renderData();
        var path = ResourceLocation.tryParse(blockEntity.getPartData().defFile().getPath().replace("/", ":"));
        if (path == null) return false;

        GeoModel geoModel = MODELS.computeIfAbsent(
                path,
                p -> GeckoLibCache.getInstance().getGeoModels().get(p)
        );

        if (geoModel == null) return false;

        var handCache = HAND_CACHE.computeIfAbsent(path, GeoMaidModelRenderer::buildHandCache);
        GeoHandRender handRender = null;
        for (var part : renderData.parts()) {
            handRender = handCache.get(part.partName());
            if (handRender != null) break;
        }

        GeoMaidRenderer renderer = RENDERERS.computeIfAbsent(
                path,
                p -> new GeoMaidRenderer(bufferSource, maidModelInfo.getTexture())
        );

        AnimatedGeoModel animated = new AnimatedGeoModel(geoModel);
        blockEntity.getPartData().expressions().getExpression("init").ifPresent(expressionMap ->
                expressionMap.forEach((partName, expression) -> {
            var part = animated.bones().get(partName);
            if(part == null)return;
            switch (expression.actionType()){
                case "hide": part.setHidden(true);
                case "show": part.setHidden(false);
            }
            part.addPositionX((float) expression.transform().x);
            part.addPositionY((float) expression.transform().y);
            part.addPositionZ((float) expression.transform().z);
            part.addRotation(new Vector3d(Math.toRadians(expression.rotation().x),
                    Math.toRadians(expression.rotation().y),
                    Math.toRadians(expression.rotation().z)));
        }));

        var renderType = RenderType.entityCutoutNoCull(maidModelInfo.getTexture());
        var vc = bufferSource.getBuffer(renderType);
        var scale = maidModelInfo.getRenderEntityScale();
        poseStack.scale(scale, scale, scale);

        renderer.render(
                animated,
                null,
                partialTick,
                renderType,
                poseStack,
                bufferSource,
                vc,
                light,
                overlay,
                1, 1, 1, 1,
                renderData.parts()
        );

        if (handRender != null) {
            renderGeoHand(blockEntity, animated, handRender, poseStack, bufferSource, light, itemInHandRenderer);
        }

        return true;
    }

    private static Map<String, GeoHandRender> buildHandCache(ResourceLocation path) {
        var geoModel = MODELS.get(path);
        var map = new HashMap<String, GeoHandRender>();
        if (geoModel == null) return map;

        var bones = new HashMap<String, GeoBone>();
        for (var bone : geoModel.topLevelBones()) {
            collectBones(bone, bones);
        }
        addHandChain(map, bones, List.of("LeftHandLocator", "LeftHand", "LeftArm"), InteractionHand.OFF_HAND);
        addHandChain(map, bones, List.of("RightHandLocator", "RightHand", "RightArm"), InteractionHand.MAIN_HAND);
        return map;
    }

    private static void collectBones(GeoBone bone, Map<String, GeoBone> bones) {
        bones.put(bone.name(), bone);
        for (var child : bone.children()) {
            collectBones(child, bones);
        }
    }

    /**
     * 从候选骨骼（locator/手/手臂）向下展开：locator 自身到手臂这一段骨骼都可以作为持物部位，
     * 公共祖先（如 UpperBody）不参与，避免多个 ragdoll 部位同时渲染物品。
     */
    private static void addHandChain(Map<String, GeoHandRender> map, Map<String, GeoBone> bones,
                                     List<String> candidates, InteractionHand interactionHand) {
        GeoBone end = null;
        for (var name : candidates) {
            end = bones.get(name);
            if (end != null) break;
        }
        if (end == null) return;

        var arm = bones.get(candidates.get(candidates.size() - 1));
        if (arm == null) arm = end;
        for (var part = end; part != null; part = part.parent()) {
            var chain = new ArrayList<String>();
            for (var p = end; ; p = p.parent()) {
                chain.add(p.name());
                if (p == part) break;
            }
            Collections.reverse(chain);
            map.putIfAbsent(part.name(), new GeoHandRender(List.copyOf(chain), interactionHand));
            if (part == arm) break;
        }
    }

    private static void renderGeoHand(MaidPartBlockEntity blockEntity, AnimatedGeoModel animated, GeoHandRender handRender,
                                      PoseStack poseStack, MultiBufferSource bufferSource, int light,
                                      ItemInHandRenderer itemInHandRenderer) {
        var entity = blockEntity.getEntity();
        if (!(entity instanceof LivingEntity maid)) return;
        var stack = maid.getItemInHand(handRender.interactionHand());
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        boolean scaleZero = false;
        var boneNames = handRender.boneNames();
        for (int i = 0; i < boneNames.size(); i++) {
            var bone = animated.bones().get(boneNames.get(i));
            if (bone == null) {
                poseStack.popPose();
                return;
            }
            if (i == boneNames.size() - 1) {
                // 最后一个骨骼按 locator 方式处理：不做 away-from-pivot，物品落在 pivot 上
                RenderUtils.translateMatrixToBone(poseStack, bone);
                RenderUtils.translateToPivotPoint(poseStack, bone);
                RenderUtils.rotateMatrixAroundBone(poseStack, bone);
                if (RenderUtils.scaleMatrixForBone(poseStack, bone)) scaleZero = true;
            } else {
                if (RenderUtils.prepMatrixForBone(poseStack, bone)) scaleZero = true;
            }
        }
        if (!scaleZero) {
            poseStack.translate(0, -0.0625, -0.1);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            itemInHandRenderer.renderItem(
                    maid, stack,
                    handRender.leftHand() ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    handRender.leftHand(), poseStack, bufferSource, light
            );
        }
        poseStack.popPose();
    }

    /**
     * 手持物品渲染信息，boneNames 为从 ragdoll 部位骨骼到手指 locator 的骨骼链。
     */
    public record GeoHandRender(List<String> boneNames, InteractionHand interactionHand) {
        public boolean leftHand() {
            return interactionHand == InteractionHand.OFF_HAND;
        }
    }
}
