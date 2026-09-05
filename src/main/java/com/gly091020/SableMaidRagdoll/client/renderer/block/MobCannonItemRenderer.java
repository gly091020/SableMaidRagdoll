package com.gly091020.SableMaidRagdoll.client.renderer.block;

import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MobCannonItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static BlockRenderDispatcher BLOCK_RENDERER;
    public static MobCannonBlockEntity fakeBlockEntity;
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public MobCannonItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        BLOCK_RENDERER = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int i, int i1) {
        if(fakeBlockEntity == null){
            fakeBlockEntity = new MobCannonBlockEntity(BlockPos.ZERO, InitBlocks.MOB_CANNON_BLOCK.get().defaultBlockState());
        }
        if(blockEntityRenderDispatcher == null){
            blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        }
        BLOCK_RENDERER.renderSingleBlock(fakeBlockEntity.getBlockState(), poseStack, bufferSource, i, i1, fakeBlockEntity.getModelData(), RenderType.CUTOUT);
        blockEntityRenderDispatcher.renderItem(fakeBlockEntity, poseStack, bufferSource, i, i1);
    }
}
