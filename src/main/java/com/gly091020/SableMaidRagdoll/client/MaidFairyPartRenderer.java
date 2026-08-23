package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.FairyType;
import com.gly091020.SableMaidRagdoll.block.parts.MaidFairyPartBlockEntity;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.gly091020.SableRagdollLib.client.renderer.AbstractPartBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MaidFairyPartRenderer extends AbstractPartBlockRenderer<MaidFairyPartBlockEntity> {
    private static final ResourceLocation[] newTextures = new ResourceLocation[19];
    private static final ResourceLocation[] babyTextures = new ResourceLocation[19];

    static {
        for (int i = 0; i < 18; i++) {
            newTextures[i] = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, String.format("textures/bedrock/entity/new_maid_fairy/maid_fairy_%d.png", i));
            babyTextures[i] = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, String.format("textures/bedrock/entity/baby_maid_fairy/maid_fairy_%d.png", i));
        }
        newTextures[18] = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/bedrock/entity/new_maid_fairy/maid_fairy_rick.png");
        babyTextures[18] = ResourceLocation.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/bedrock/entity/baby_maid_fairy/maid_fairy_rick.png");
    }

    @Override
    public void renderMain(MaidFairyPartBlockEntity blockEntity, float delta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        var cache = MaidPartRenderCache.fairy(blockEntity.getModelType());
        if(cache.model() == null)return;
        cache.reset();

        var vc = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(getTexture(blockEntity.getFairyType(), blockEntity.getModelType(), blockEntity.isRick())));

        poseStack.pushPose();

        var data = blockEntity.getPartData();
        for (var part: cache.parts(data.defFile(), data.partName(), data.renderData().parts())){
            if (part.flatChild()) {
                MaidModelHelper.renderBedrockPartsWithFlat(part.part(), poseStack, vc, light, overlay);
            } else {
                MaidModelHelper.renderBedrockPart(part.part(), poseStack, vc, light, overlay);
            }
        }

        poseStack.popPose();
    }

    public static ResourceLocation getTexture(FairyType fairyType, MaidFairyPartBlockEntity.ModelType modelType, boolean isRick){
        if(modelType == MaidFairyPartBlockEntity.ModelType.BABY){
            if(isRick)return babyTextures[18];
            else return babyTextures[fairyType.ordinal()];
        }else{
            if(isRick)return newTextures[18];
            else return newTextures[fairyType.ordinal()];
        }
    }

    @Override
    public void transformBefore(MaidFairyPartBlockEntity blockEntity, PoseStack poseStack) {
        var shape = blockEntity.getShape();
        var height = shape.bounds().maxY - shape.bounds().minY;
        poseStack.translate(0.5, 1.5d + (1 - height) / 2, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
    }
}
