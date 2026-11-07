package com.gly091020.SableMaidRagdoll.client.renderer.block;

import com.gly091020.SableMaidRagdoll.block.giant_slime.GiantSlimeBlock;
import com.gly091020.SableMaidRagdoll.block.giant_slime.GiantSlimeBlockEntity;
import com.gly091020.SableMaidRagdoll.block.giant_slime.GiantSlimePart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class GiantSlimeBlockRenderer implements BlockEntityRenderer<GiantSlimeBlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public GiantSlimeBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(GiantSlimeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var state = blockEntity.getBlockState();
        if (!state.hasProperty(GiantSlimeBlock.PART)
                || state.getValue(GiantSlimeBlock.PART) != GiantSlimePart.CORE) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-GiantSlimeBlock.HORIZONTAL_RADIUS, -GiantSlimeBlock.HORIZONTAL_RADIUS,
                -GiantSlimeBlock.HORIZONTAL_RADIUS);
        poseStack.scale(GiantSlimeBlock.SIZE, GiantSlimeBlock.SIZE, GiantSlimeBlock.SIZE);
        context.getBlockRenderDispatcher().renderSingleBlock(
                Blocks.SLIME_BLOCK.defaultBlockState(), poseStack, bufferSource,
                packedLight, packedOverlay, blockEntity.getModelData(), RenderType.translucent());
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(GiantSlimeBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(
                pos.getX() - GiantSlimeBlock.HORIZONTAL_RADIUS,
                pos.getY() - GiantSlimeBlock.HORIZONTAL_RADIUS,
                pos.getZ() - GiantSlimeBlock.HORIZONTAL_RADIUS,
                pos.getX() + GiantSlimeBlock.HORIZONTAL_RADIUS + 1,
                pos.getY() + GiantSlimeBlock.HORIZONTAL_RADIUS + 1,
                pos.getZ() + GiantSlimeBlock.HORIZONTAL_RADIUS + 1
        );
    }
}
