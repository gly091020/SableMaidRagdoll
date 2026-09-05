package com.gly091020.SableMaidRagdoll.client.renderer.block;

import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class MobCannonRenderer implements BlockEntityRenderer<MobCannonBlockEntity> {
    private final BlockEntityRendererProvider.Context context;
    public MobCannonRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(MobCannonBlockEntity mobCannonBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        poseStack.pushPose();
        context.getBlockRenderDispatcher().renderSingleBlock(Blocks.DARK_OAK_SLAB.defaultBlockState(), poseStack, multiBufferSource, i, i1);
        poseStack.translate(0, 0.5, 0);
        context.getBlockRenderDispatcher().renderSingleBlock(Blocks.OAK_FENCE.defaultBlockState(), poseStack, multiBufferSource, i, i1);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) mobCannonBlockEntity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) mobCannonBlockEntity.getXRot()));
        poseStack.translate(-0.5, -0.5, -0.5);
        context.getBlockRenderDispatcher().renderSingleBlock(Blocks.DISPENSER.defaultBlockState(), poseStack, multiBufferSource, i, i1);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MobCannonBlockEntity blockEntity) {
        return AABB.ofSize(blockEntity.getBlockPos().getCenter(), 1.5, 2.5, 1.5);
    }
}
