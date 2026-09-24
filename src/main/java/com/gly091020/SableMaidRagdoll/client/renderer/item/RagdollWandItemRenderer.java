package com.gly091020.SableMaidRagdoll.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

/**
 * 布娃娃魔杖的渲染：一个动态的末影水晶，中间的方块换成绑定的布娃娃。
 * 只保留原版水晶的外壳，去掉了底座，中间的 cube 换成布娃娃。
 */
@OnlyIn(Dist.CLIENT)
public class RagdollWandItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation END_CRYSTAL_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType CRYSTAL_RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_TEXTURE);
    private static final float SIN_45 = (float) Math.sin(Math.PI / 4.0);
    /** 外壳两层之间那 60° 的错开，和原版一致。 */
    private static final Quaternionf CRYSTAL_ROTATION = new Quaternionf().setAngleAxis((float) (Math.PI / 3.0), SIN_45, 0.0F, SIN_45);
    /** 平时自转速度（度/tick）。 */
    private static final float IDLE_SPIN = 3.0F;
    /** 整个蓄力过程额外旋转的总角度（度），按蓄力进度的平方分布，转速会渐进变快。 */
    private static final float CHARGE_SPIN_ANGLE = 540.0F;
    /** 漂浮幅度。 */
    private static final float BOB_AMPLITUDE = 0.1F;
    /** 物品拿在手上/摆在地上时的整体缩放。 */
    private static final float DISPLAY_SCALE = 0.55F;
    /** 布娃娃在水晶核心里的缩放，太大就会穿出外壳。 */
    private static final float CORE_SCALE = 0.8F;
    /** 居中布娃娃用：抵消 PlayerCheatDeathItemRenderer 里的 0~1 方块空间偏移。 */
    private static final float CORE_OFFSET_XZ = -0.5F;
    private static final float CORE_OFFSET_Y = -0.42F;

    private final EntityModelSet modelSet;
    private final PlayerCheatDeathItemRenderer dollRenderer;
    // 客户端资源重载后模型集才会填充，所以水晶模型要延迟到第一次渲染时再 bake
    private ModelPart glass;

    public RagdollWandItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        this.modelSet = modelSet;
        this.dollRenderer = new PlayerCheatDeathItemRenderer(dispatcher, modelSet);
    }

    private void ensureCrystalModel() {
        if (this.glass != null) return;
        this.glass = this.modelSet.bakeLayer(ModelLayers.END_CRYSTAL).getChild("glass");
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        // 物品栏里不做漂浮动画
        boolean bob = context != ItemDisplayContext.GUI && context != ItemDisplayContext.FIXED;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        renderCrystal(stack, poseStack, bufferSource, light, overlay, getTime(partialTick), getCharge(stack, partialTick), bob);
        poseStack.popPose();
    }

    /**
     * 渲染水晶本体，调用方负责摆放位置与缩放。
     *
     * @param time  用于自转的时间
     * @param charge 蓄力进度 0~1，蓄力时自转逐渐加快并且使用满亮度
     * @param bob   是否做上下的漂浮动画
     */
    public void renderCrystal(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, float time, float charge, boolean bob) {
        ensureCrystalModel();
        float spin = getSpin(time, charge);
        int renderLight = charge > 0.0F ? LightTexture.FULL_BRIGHT : light;
        VertexConsumer consumer = bufferSource.getBuffer(CRYSTAL_RENDER_TYPE);
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        if (bob) poseStack.translate(0.0F, getBob(time), 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(CRYSTAL_ROTATION);
        this.glass.render(poseStack, consumer, renderLight, overlay);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(CRYSTAL_ROTATION);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        this.glass.render(poseStack, consumer, renderLight, overlay);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(CRYSTAL_ROTATION);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        // 原版这里渲染水晶中间的方块，这里换成布娃娃
        renderCore(stack, poseStack, bufferSource, renderLight, overlay);
        poseStack.popPose();
    }

    private void renderCore(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        poseStack.pushPose();
        poseStack.scale(CORE_SCALE, -CORE_SCALE, CORE_SCALE);
        poseStack.translate(CORE_OFFSET_XZ, CORE_OFFSET_Y, CORE_OFFSET_XZ);
        this.dollRenderer.renderDoll(stack, poseStack, bufferSource, light, overlay);
        poseStack.popPose();
    }

    /** 当前蓄力进度（0~1），用 partialTick 插值，没有在蓄力时为 0。 */
    public static float getCharge(ItemStack stack, float partialTick) {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.isUsingItem() || !player.getUseItem().is(stack.getItem())) return 0.0F;
        int duration = stack.getUseDuration(player);
        if (duration <= 0) return 0.0F;
        float used = duration - (player.getUseItemRemainingTicks() - partialTick + 1.0F);
        return Mth.clamp(used / duration, 0.0F, 1.0F);
    }

    public static float getTime(float partialTick) {
        var level = Minecraft.getInstance().level;
        return level == null ? 0.0F : level.getGameTime() + partialTick;
    }

    /** 自转角度。蓄力时额外角度按进度平方增长，转速从 0 平滑加速到最大。 */
    private static float getSpin(float time, float charge) {
        float spin = time * IDLE_SPIN;
        if (charge <= 0.0F) return spin;
        return spin + CHARGE_SPIN_ANGLE * charge * charge;
    }

    private static float getBob(float time) {
        return Mth.sin(time * 0.2F) * BOB_AMPLITUDE;
    }
}
