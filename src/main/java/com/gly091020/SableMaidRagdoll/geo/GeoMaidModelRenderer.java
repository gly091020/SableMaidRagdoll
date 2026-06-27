package com.gly091020.SableMaidRagdoll.geo;

import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GeoMaidModelRenderer {
    private static final Map<ResourceLocation, GeoModel> MODELS = new HashMap<>();
    private static final Map<ResourceLocation, GeoMaidRenderer> RENDERERS = new HashMap<>();

    public static boolean render(@NotNull MaidPartBlockEntity blockEntity,
                                 float partialTick,
                                 @NotNull PoseStack poseStack,
                                 @NotNull MultiBufferSource bufferSource,
                                 int light,
                                 int overlay,
                                 MaidModelInfo maidModelInfo) {

        var renderData = blockEntity.getRenderData();
        var path = ResourceLocation.tryParse(renderData.modelName());
        if (path == null) return false;

        GeoModel geoModel = MODELS.computeIfAbsent(
                path,
                p -> GeckoLibCache.getInstance().getGeoModels().get(p)
        );

        if (geoModel == null) return false;

        GeoMaidRenderer renderer = RENDERERS.computeIfAbsent(
                path,
                p -> new GeoMaidRenderer(bufferSource, maidModelInfo.getTexture(), blockEntity.getHidePart())
        );

        var parts = new ArrayList<String>();
        if(renderData.partName().contains("|"))
            parts.addAll(Arrays.asList(renderData.partName().split("\\|")));
        else{
            parts.add(renderData.partName());
        }
        if(parts.isEmpty()){
            return false;
        }

        AnimatedGeoModel animated = new AnimatedGeoModel(geoModel);
        var renderType = RenderType.entityCutoutNoCull(maidModelInfo.getTexture());
        var vc = bufferSource.getBuffer(renderType);
        var shape = blockEntity.getShape();
        var height = shape.bounds().maxY - shape.bounds().minY;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5d + (1 - height) / 2, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        poseStack.translate(renderData.transform().x, renderData.transform().y, renderData.transform().z);
        poseStack.mulPose(new Quaternionf().rotateXYZ((float) Math.toRadians(renderData.rotate().x),
                (float) Math.toRadians(renderData.rotate().y),
                (float) Math.toRadians(renderData.rotate().z)));

        HardCodeModelHandler.called(path, animated);

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
                parts,
                renderData.flatChild(),
                renderData.extraPart()
        );

        poseStack.popPose();
        return true;
    }
}
