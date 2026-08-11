package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.listener.EmojiReloadListener;
import com.github.tartaricacid.touhoulittlemaid.compat.gun.common.GunClientUtil;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.gly091020.SableMaidRagdoll.util.RagdollChatBubbleRenderer;
import com.gly091020.SableMaidRagdoll.util.RagdollEmoji;
import com.gly091020.SableRagdollLib.client.renderer.AbstractPartBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.Random;

public class MaidPartRenderer extends AbstractPartBlockRenderer<MaidPartBlockEntity> {
    private final ItemInHandRenderer itemInHandRenderer;

    public MaidPartRenderer(ItemInHandRenderer itemInHandRenderer) {
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void renderMain(MaidPartBlockEntity blockEntity, float delta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        var data = blockEntity.getPartData();
        if (!data.defFile().getNamespace().equals(SableMaidRagdoll.MODID)) return;

        var cache = MaidPartRenderCache.get(data.defFile());
        var modelInfo = cache.info();
        if (modelInfo == null) return;
        if (GeoMaidModelRenderer.render(blockEntity, delta, poseStack, multiBufferSource, light, overlay, modelInfo, this.itemInHandRenderer)) return;

        if (cache.model() == null) return;

        cache.reset();
        cache.applyInit(data.expressions());

        poseStack.pushPose();
        var vc = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(cache.texture()));
        var scale = cache.scale();
        poseStack.scale(scale, scale, scale);

        for (var renderPart : cache.parts(data.partName(), data.renderData().parts())) {
            if (renderPart.flatChild()) {
                MaidModelHelper.renderBedrockPartsWithFlat(renderPart.part(), poseStack, vc, light, overlay);
            } else {
                MaidModelHelper.renderBedrockPart(renderPart.part(), poseStack, vc, light, overlay);
            }
        }
        renderHand(blockEntity, cache, poseStack, multiBufferSource, light);

        poseStack.popPose();
    }

    private void renderHand(MaidPartBlockEntity blockEntity, MaidPartRenderCache.Entry cache, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        var entity = blockEntity.getEntity();
        if (!(entity instanceof LivingEntity maid)) return;
        var handRender = cache.handRender(blockEntity.getPartData().partName());
        if (handRender == null) return;
        var stack = maid.getItemInHand(handRender.interactionHand());
        if (stack.isEmpty()) return;
        var model = cache.model();
        if (model == null) return;

        poseStack.pushPose();
        // 从 ragdoll 部位沿层级变换到实际手臂
        transformToSelf(handRender.parent(), handRender.hand(), poseStack);
        var side = handRender.leftHand() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        if (model.hasArmPositioningModel(side)) {
            model.translateToPositioningHand(side, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0, 0.125, -0.0625);
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate((handRender.leftHand() ? -1 : 1) / 16.0, 0.125, -0.525);
        }
        GunClientUtil.addItemTranslate(poseStack, stack, handRender.leftHand());
        itemInHandRenderer.renderItem(
                maid, stack,
                handRender.leftHand() ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                handRender.leftHand(), poseStack, multiBufferSource, light
        );
        poseStack.popPose();
    }

    private static void transformToSelf(BedrockPart self, BedrockPart part, PoseStack poseStack) {
        if (part.parent != null && part != self) {
            transformToSelf(self, part.parent, poseStack);
        }
        part.translateAndRotate(poseStack);
    }

    @Override
    public void transformBefore(MaidPartBlockEntity blockEntity, PoseStack poseStack) {
        var shape = blockEntity.getShape();
        var height = shape.bounds().maxY - shape.bounds().minY;
        poseStack.translate(0.5, 1.5d + (1 - height) / 2, 0.5);
        if(GeoMaidModelRenderer.isGeo(blockEntity)){
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            return;
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
    }

    public static final ResourceLocation STAR = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "textures/star.png");
    public static void renderStars(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 center, float partialTick, int count, float radius, float size, int light) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(STAR));
        poseStack.pushPose();
        poseStack.translate(center.x, center.y, center.z);
        Random random = new Random(114514L);
        float time = (Minecraft.getInstance().level.getGameTime() + partialTick) * 0.15f;
        for (int i = 0; i < count; i++) {
            double angle = time + i * Math.PI * 2 / count;
            var r = radius + random.nextFloat() / 10;
            float x = (float) Math.cos(angle) * r;
            float z = (float) Math.sin(angle) * r;
            poseStack.pushPose();
            poseStack.translate(x, 0.3f + random.nextFloat() * 0.1, z);
            Vec3 starPos = center.add(x, 0.3f, z);
            double dx = center.x - starPos.x;
            double dz = center.z - starPos.z;
            float yaw = (float) Math.atan2(dx, dz);
            poseStack.mulPose(Axis.YP.rotation(yaw));
            poseStack.mulPose(Axis.ZP.rotation(random.nextFloat() * 0.5f));
            Matrix4f matrix = poseStack.last().pose();
            consumer.addVertex(matrix, -size, -size, 0).setUv(0, 0).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            consumer.addVertex(matrix, size, -size, 0).setUv(1, 0).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            consumer.addVertex(matrix, size, size, 0).setUv(1, 1).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            consumer.addVertex(matrix, -size, size, 0).setUv(0, 1).setNormal(0, 0, 1).setColor(255, 255, 255, 255).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public void firstRender(MaidPartBlockEntity blockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        if(blockEntity.getPartData().partName().contains("head") && blockEntity.getEntity() != null) {
            var height = blockEntity.getShape().max(Direction.Axis.Y) - blockEntity.getShape().min(Direction.Axis.Y);
            renderChatImage(blockEntity, poseStack, multiBufferSource, new Vec3(0.5, height + 0.5, 0.5), i, v);
            if(blockEntity.getEntity() instanceof Player)return;
            renderStars(poseStack, multiBufferSource, Vec3.ZERO.add(0.5, 0.5 - height / 2, 0.5), v, 10, 0.5f, 0.1f, LightTexture.FULL_BRIGHT);
        }
    }

    public static void renderChatImage(MaidPartBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource bufferSource, Vec3 pos, int light, float partialTick) {
        poseStack.pushPose();

        Vec3 worldPos = SableCompanion.INSTANCE.projectOutOfSubLevel(
                blockEntity.getLevel(), (Position) blockEntity.getBlockPos().getCenter());
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double dx = cameraPos.x - worldPos.x;
        double dy = cameraPos.y - worldPos.y;
        double dz = cameraPos.z - worldPos.z;
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(blockEntity.getLevel(), blockEntity.getBlockPos());
        if (subLevel != null) {
            Vector3d dir = new Vector3d(dx, dy, dz);
            subLevel.logicalPose().orientation().transformInverse(dir);
            dx = dir.x;
            dz = dir.z;
        }
        poseStack.translate(pos.x, pos.y, pos.z);
        poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(-dx, -dz)));
        poseStack.scale(-1.0F, 1.0F, 1.0F);
        EmojiReloadListener.EmojiResource emoji = resolveEmoji(blockEntity);
        if (emoji == null) {
            poseStack.popPose();
            return;
        }
        RagdollChatBubbleRenderer.render(emoji, poseStack, bufferSource, light, partialTick);
        poseStack.popPose();
    }

    /**
     * 解析当前气泡要显示的表情。
     * 只有玩家明确设置过表情才会渲染：设置为空（已清除）或从未设置都返回 null（不渲染）。
     */
    private static EmojiReloadListener.EmojiResource resolveEmoji(MaidPartBlockEntity blockEntity) {
        var entity = blockEntity.getEntity();
        if (!(entity instanceof Player player) || !RagdollEmoji.hasEmoji(player)) return null;
        ResourceLocation location = RagdollEmoji.getEmoji(player);
        if (location.equals(SableMaidRagdoll.EMPTY_EMOJI)) return null;
        for (var emoji : RagdollChatBubbleRenderer.getEmojis()) {
            if (emoji.location().equals(location)) return emoji;
        }
        return null;
    }
}
