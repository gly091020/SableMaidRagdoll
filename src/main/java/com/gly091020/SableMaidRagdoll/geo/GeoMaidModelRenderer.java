package com.gly091020.SableMaidRagdoll.geo;

import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.resource.GeckoLibCache;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class GeoMaidModelRenderer {
    private static final Map<ResourceLocation, GeoModel> MODELS = new HashMap<>();
    private static final Map<ResourceLocation, GeoMaidRenderer> RENDERERS = new HashMap<>();

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
                                 MaidModelInfo maidModelInfo) {

        var renderData = blockEntity.getPartData().renderData();
        var path = ResourceLocation.tryParse(blockEntity.getPartData().defFile().getPath().replace("/", ":"));
        if (path == null) return false;

        GeoModel geoModel = MODELS.computeIfAbsent(
                path,
                p -> GeckoLibCache.getInstance().getGeoModels().get(p)
        );

        if (geoModel == null) return false;

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

        return true;
    }
}
