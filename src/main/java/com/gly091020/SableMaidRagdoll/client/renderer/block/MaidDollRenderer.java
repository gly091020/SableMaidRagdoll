package com.gly091020.SableMaidRagdoll.client.renderer.block;

import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollBlockEntity;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableMaidRagdoll.maid.api.MaidSoundType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class MaidDollRenderer implements BlockEntityRenderer<MaidDollBlockEntity> {
    private final BlockEntityRendererProvider.Context context;
    public MaidDollRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(MaidDollBlockEntity maidDollBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        var modelID = maidDollBlockEntity.getModelID();
        var level = maidDollBlockEntity.getLevel();
        if(modelID.isEmpty() || level == null)return;

        Entity maid = MaidRagdollTypesManager.getRenderEntity(maidDollBlockEntity.getLevel(), maidDollBlockEntity.getMaidDollData());
        if(maid == null)return;

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(1, 0, 1);

        double d = (System.currentTimeMillis() - maidDollBlockEntity.lastPat) / 1000D;
        if(d <= 0.2) {
            poseStack.scale(1, (float) ((Math.cos(d / 0.1d * Math.PI) + 1) * 0.5 * 0.25 + 0.75), 1);
        }
        poseStack.scale(1.3f, 1.3f, 1.3f);
        if(maidDollBlockEntity.triggerPat){
            maidDollBlockEntity.triggerPat = false;
            var pos = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) maidDollBlockEntity.getBlockPos().getCenter());
            MaidRagdollTypesManager.playSound(maidDollBlockEntity.getRagdollTypeID(), level, pos, maidDollBlockEntity.getSoundID(), MaidSoundType.IDLE, 0.5f);
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                    SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1, 1, level.random, maidDollBlockEntity.getBlockPos()
            ));
        }

        switch (maidDollBlockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING)) {
            case EAST:
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case WEST:
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                break;
            case SOUTH:
                break;
            case NORTH:
            default:
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
        }

        EntityRenderDispatcher render = context.getEntityRenderer();
        boolean isShowHitBox = render.shouldRenderHitBoxes();
        render.setRenderShadow(false);
        render.setRenderHitBoxes(false);
        render.render(maid, 0, 0, 0, 0, 0,
                poseStack, multiBufferSource, light);
        render.setRenderHitBoxes(isShowHitBox);
        render.setRenderShadow(true);
        poseStack.popPose();
    }
}
