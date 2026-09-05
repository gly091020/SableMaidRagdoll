package com.gly091020.SableMaidRagdoll.client.renderer.item;

import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil;
import com.gly091020.SableMaidRagdoll.init.InitDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PlayerCheatDeathItemRenderer extends BlockEntityWithoutLevelRenderer {
    public PlayerCheatDeathItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        var level = Minecraft.getInstance().level;
        if(level == null)return;
        var modelID = stack.get(InitDataComponents.MAID_MODEL);
        EntityMaid maid;
        try {
            maid = (EntityMaid) EntityCacheUtil.ENTITY_CACHE.get(EntityMaid.TYPE, () -> {
                Entity e = EntityMaid.TYPE.create(level);
                return Objects.requireNonNullElseGet(e, () -> new EntityMaid(level));
            });
        } catch (ExecutionException | ClassCastException ignored) {return;}
        EntityCacheUtil.clearMaidDataResidue(maid, true);
        if(modelID == null)
            maid.setCustomName(Component.literal("=>").append(Minecraft.getInstance().getGameProfile().getName()));
        else maid.setModelId(modelID);
        maid.renderState = MaidRenderState.GARAGE_KIT;
        maid.setInSittingPose(true);

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(1, 0, 1);
        EntityRenderDispatcher render = Minecraft.getInstance().getEntityRenderDispatcher();
        boolean isShowHitBox = render.shouldRenderHitBoxes();
        render.setRenderShadow(false);
        render.setRenderHitBoxes(false);
        render.render(maid, 0, 0, 0, 0, 0,
                poseStack, bufferSource, light);
        render.setRenderHitBoxes(isShowHitBox);
        render.setRenderShadow(true);
        poseStack.popPose();
    }
}
