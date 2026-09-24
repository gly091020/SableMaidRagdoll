package com.gly091020.SableMaidRagdoll.client.renderer;

import com.gly091020.SableMaidRagdoll.client.renderer.item.RagdollWandItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * 布娃娃魔杖的第一人称手部渲染：空闲时单手托着水晶，蓄力时双手向前平举、水晶浮在双手中间。
 * <p>
 * 原版对非空手不渲染手臂，所以这里要自己画。下面的常量都是镜头坐标系里的手感参数（1 = 1 格）。
 */
@OnlyIn(Dist.CLIENT)
public final class RagdollWandHandRenderer {
    // ===== 空闲持握：沿用原版第一人称手臂的位置，水晶放在手心 =====
    private static final float IDLE_HAND_X = 0.64F;
    private static final float IDLE_HAND_Y = -0.6F;
    private static final float IDLE_HAND_Z = -0.72F;
    private static final float IDLE_HAND_YAW = 45.0F;
    private static final float IDLE_HAND_PITCH = 0.0F;
    private static final float IDLE_HAND_ROLL = 0.0F;
    private static final float IDLE_CRYSTAL_X = 0.5F;
    private static final float IDLE_CRYSTAL_Y = -0.5F;
    private static final float IDLE_CRYSTAL_Z = -1;
    private static final float IDLE_CRYSTAL_SCALE = 0.3F;
    private static final float IDLE_CRYSTAL_PITCH = -20.0F;

    // ===== 施法（蓄力）：双手向中间合拢并抬平 =====
    private static final float CAST_HAND_X = 0.35F;
    private static final float CAST_HAND_Y = -0.55F;
    private static final float CAST_HAND_Z = -0.70F;
    private static final float CAST_HAND_YAW = 45.0F;
    private static final float CAST_HAND_PITCH = -30.0F;
    private static final float CAST_HAND_ROLL = 0.0F;
    private static final float CAST_CRYSTAL_Y = -0.38F;
    private static final float CAST_CRYSTAL_Z = -0.95F;
    private static final float CAST_CRYSTAL_SCALE = 0.35F;
    private static final float CAST_CRYSTAL_PITCH = -12.0F;

    private RagdollWandHandRenderer() {
    }

    /**
     * @param casting 是否正在蓄力（蓄力时画两只手，空闲时只画拿魔杖的那只手）
     * @param charge  蓄力进度 0~1
     */
    public static void render(RagdollWandItemRenderer crystalRenderer, RenderHandEvent event, ItemStack stack, HumanoidArm arm, boolean casting, float charge) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float time = RagdollWandItemRenderer.getTime(event.getPartialTick());

        if (casting) {
            renderArm(poseStack, bufferSource, light, player, HumanoidArm.RIGHT,
                    CAST_HAND_X, CAST_HAND_Y, CAST_HAND_Z, CAST_HAND_YAW, CAST_HAND_PITCH, CAST_HAND_ROLL);
            renderArm(poseStack, bufferSource, light, player, HumanoidArm.LEFT,
                    CAST_HAND_X, CAST_HAND_Y, CAST_HAND_Z, CAST_HAND_YAW, CAST_HAND_PITCH, CAST_HAND_ROLL);

            poseStack.pushPose();
            poseStack.translate(0.0F, CAST_CRYSTAL_Y, CAST_CRYSTAL_Z);
            poseStack.mulPose(Axis.XP.rotationDegrees(CAST_CRYSTAL_PITCH));
            poseStack.scale(CAST_CRYSTAL_SCALE, CAST_CRYSTAL_SCALE, CAST_CRYSTAL_SCALE);
            crystalRenderer.renderCrystal(stack, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, time, charge, true);
            poseStack.popPose();
            return;
        }

        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        renderArm(poseStack, bufferSource, light, player, arm,
                IDLE_HAND_X, IDLE_HAND_Y, IDLE_HAND_Z, IDLE_HAND_YAW, IDLE_HAND_PITCH, IDLE_HAND_ROLL);
        poseStack.pushPose();
        poseStack.translate(side * IDLE_CRYSTAL_X, IDLE_CRYSTAL_Y, IDLE_CRYSTAL_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(IDLE_CRYSTAL_PITCH));
        poseStack.scale(IDLE_CRYSTAL_SCALE, IDLE_CRYSTAL_SCALE, IDLE_CRYSTAL_SCALE);
        crystalRenderer.renderCrystal(stack, poseStack, bufferSource, light, OverlayTexture.NO_OVERLAY, time, 0.0F, true);
        poseStack.popPose();
    }

    private static void renderArm(PoseStack poseStack, MultiBufferSource bufferSource, int light, LocalPlayer player, HumanoidArm arm,
                                  float x, float y, float z, float yaw, float pitch, float roll) {
        boolean right = arm == HumanoidArm.RIGHT;
        float side = right ? 1.0F : -1.0F;
        var renderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();

        poseStack.pushPose();
        poseStack.translate(side * x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * roll));
        // 原版第一人称手臂的枢轴修正，去掉这段手臂会跑到奇怪的位置
        poseStack.translate(side * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -135.0F));
        poseStack.translate(side * 5.6F, 0.0F, 0.0F);

        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        ModelPart armPart = right ? model.rightArm : model.leftArm;
        ModelPart sleeve = right ? model.rightSleeve : model.leftSleeve;
        armPart.xRot = 0.0F;
        armPart.yRot = 0.0F;
        armPart.zRot = 0.0F;
        sleeve.xRot = 0.0F;
        sleeve.yRot = 0.0F;
        sleeve.zRot = 0.0F;

        var skin = player.getSkin().texture();
        armPart.render(poseStack, bufferSource.getBuffer(RenderType.entitySolid(skin)), light, OverlayTexture.NO_OVERLAY);
        sleeve.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(skin)), light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
