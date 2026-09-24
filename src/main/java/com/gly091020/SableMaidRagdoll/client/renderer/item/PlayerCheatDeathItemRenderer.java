package com.gly091020.SableMaidRagdoll.client.renderer.item;

import com.gly091020.SableMaidRagdoll.client.model.MaidDollDefaultModel;
import com.gly091020.SableMaidRagdoll.init.InitDataComponents;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PlayerCheatDeathItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final EntityModelSet modelSet;
    private MaidDollDefaultModel<Entity> defaultModel;

    public PlayerCheatDeathItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.modelSet = modelSet;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        renderDoll(stack, poseStack, bufferSource, light, overlay);
    }

    /**
     * 渲染玩偶本体：绑定了模型就渲染对应女仆实体，没有绑定就渲染默认玩偶模型。
     * 供其它渲染器复用（例如布娃娃魔杖把水晶中间的方块换成玩偶）。
     */
    public void renderDoll(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        var level = Minecraft.getInstance().level;
        if(level == null)return;
        var data = stack.get(InitDataComponents.MAID_DOLL_DATA);
        if(data == null){
            renderDefaultModel(poseStack, bufferSource, light, overlay);
            return;
        }
        var entity = MaidRagdollTypesManager.getRenderEntity(level, data);
        if(entity == null){
            renderDefaultModel(poseStack, bufferSource, light, overlay);
            return;
        }

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(1, 0, 1);
        EntityRenderDispatcher render = Minecraft.getInstance().getEntityRenderDispatcher();
        boolean isShowHitBox = render.shouldRenderHitBoxes();
        render.setRenderShadow(false);
        render.setRenderHitBoxes(false);
        render.render(entity, 0, 0, 0, 0, 0,
                poseStack, bufferSource, light);
        render.setRenderHitBoxes(isShowHitBox);
        render.setRenderShadow(true);
        poseStack.popPose();
    }

    private void renderDefaultModel(PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        var player = Minecraft.getInstance().player;
        if(player == null)return;
        if(defaultModel == null)
            defaultModel = new MaidDollDefaultModel<>(modelSet.bakeLayer(MaidDollDefaultModel.LAYER_LOCATION));

        ResourceLocation texture = player.getSkin().texture();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        poseStack.pushPose();
        poseStack.translate(0.5, 0.8, 0.5);
        poseStack.scale(0.65f, -0.65f, -0.65f);
        defaultModel.renderToBuffer(poseStack, consumer, light, overlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
