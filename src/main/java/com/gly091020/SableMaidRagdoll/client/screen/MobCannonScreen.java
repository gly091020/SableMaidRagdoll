package com.gly091020.SableMaidRagdoll.client.screen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.menu.MobCannonMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MobCannonScreen extends AbstractContainerScreen<MobCannonMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "textures/gui/mob_cannon.png");
    private static final int BACKGROUND_WIDTH = 177;
    private static final int BACKGROUND_HEIGHT = 168;

    private static final int CANNON_X = 25;
    private static final int CANNON_Y = 45;
    private static final int CANNON_SCALE = 20;
    private static final int CANNON_X_ROT = 35;
    private static final double CANNON_Y_ROT = 45;

    private static MobCannonBlockEntity preview;

    public MobCannonScreen(MobCannonMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = BACKGROUND_WIDTH;
        imageHeight = BACKGROUND_HEIGHT;
        inventoryLabelY = 74;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(BACKGROUND, x, y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        renderCannon(guiGraphics, leftPos, topPos);
    }

    private void renderCannon(GuiGraphics guiGraphics, int left, int top) {
        Minecraft mc = Minecraft.getInstance();
        MobCannonBlockEntity cannon = getPreview();
        BlockEntityRenderer<MobCannonBlockEntity> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(cannon);
        if (renderer == null) return;
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(left + CANNON_X + CANNON_SCALE / 2.0, top + CANNON_Y + CANNON_SCALE / 2.0, 100);
        poseStack.scale(CANNON_SCALE, -CANNON_SCALE, CANNON_SCALE);
        poseStack.translate(-0.5, -0.5, -0.5);
        renderer.render(cannon, 0.0F, poseStack, guiGraphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static MobCannonBlockEntity getPreview() {
        if (preview == null) {
            preview = new MobCannonBlockEntity(BlockPos.ZERO, InitBlocks.MOB_CANNON_BLOCK.get().defaultBlockState());
            preview.setXRot(CANNON_X_ROT);
            preview.setYRot(CANNON_Y_ROT);
        }
        return preview;
    }
}
