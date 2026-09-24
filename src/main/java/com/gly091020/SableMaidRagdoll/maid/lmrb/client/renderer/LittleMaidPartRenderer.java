package com.gly091020.SableMaidRagdoll.maid.lmrb.client.renderer;

import com.gly091020.SableMaidRagdoll.maid.lmrb.block.LittleMaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.maid.tlm.client.renderer.MaidPartRenderer;
import com.gly091020.SableRagdollLib.client.renderer.AbstractPartBlockRenderer;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import com.gly091020.SableRagdollLib.resource.file.RagdollExpressions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.sistr.littlemaidmodelloader.maidmodel.ModelMultiBase;
import net.sistr.littlemaidmodelloader.maidmodel.ModelRenderer;
import net.sistr.littlemaidmodelloader.multimodel.layer.MMRenderContext;
import net.sistr.littlemaidmodelloader.resource.holder.TextureHolder;
import net.sistr.littlemaidmodelloader.resource.manager.LMTextureManager;
import net.sistr.littlemaidmodelloader.resource.util.TextureColors;
import net.sistr.littlemaidrebirth.LMRBMod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LMML 女仆布娃娃部位渲染器。
 * <p>
 * 渲染约定：部位方块中心对应模型部件自身的原点，模型空间（y 向下）经绕 X 轴翻转后
 * 与方块空间一致，因此 {@code hitbox} 可以直接由模型盒子的坐标换算得到。
 */
public class LittleMaidPartRenderer extends AbstractPartBlockRenderer<LittleMaidPartBlockEntity> {
    private static final float SCALE = 0.0625F;

    @Override
    public void transformBefore(LittleMaidPartBlockEntity blockEntity, PoseStack poseStack) {
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
    }

    /** 头顶星星，效果与 TLM 女仆布娃娃一致。 */
    @Override
    public void firstRender(LittleMaidPartBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                            MultiBufferSource bufferSource, int light, int overlay) {
        if (blockEntity.getPartData() == null || !blockEntity.getPartData().partName().contains("head"))
            return;
        if (blockEntity.getEntity() == null || blockEntity.getEntity() instanceof Player)
            return;
        var shape = blockEntity.getShape();
        var height = shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y);
        MaidPartRenderer.renderStars(poseStack, bufferSource, new Vec3(0.5, 0.5 - height / 2, 0.5),
                partialTick, 10, 0.5F, 0.1F, LightTexture.FULL_BRIGHT);
    }

    @Override
    public void renderMain(LittleMaidPartBlockEntity blockEntity, float delta, PoseStack poseStack,
                           MultiBufferSource bufferSource, int light, int overlay) {
        var data = blockEntity.getPartData();
        if (data == null || !data.defFile().getNamespace().equals(LMRBMod.MODID))
            return;
        if (data.renderData().parts().isEmpty())
            return;

        // 模型名取自贴图包，这样共用同一套定义的 ModelLittleMaid_* 系列能渲染各自的模型
        var entry = LittleMaidPartRenderCache.get(modelNameOf(blockEntity));
        if (entry.model() == null)
            entry = LittleMaidPartRenderCache.get(data.defFile().getPath());
        if (entry.model() == null)
            return;

        var texture = resolveTexture(blockEntity);
        if (texture == null)
            return;

        var vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        new MMRenderContext(poseStack, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F)
                .render(ModelRenderer::setParam);

        var init = resolveInitExpressions(blockEntity, entry);
        ModelRenderer anchor = null;
        for (var part : data.renderData().parts()) {
            var node = entry.node(part.partName());
            if (node == null)
                continue;
            if (anchor == null)
                anchor = node;
            renderNode(node, anchor, part.flatChild(), poseStack, init);
        }
        if (anchor != null)
            renderHeldItems(blockEntity, entry.model(), anchor, poseStack, bufferSource, light);
    }

    /**
     * 手持物：按 LMML 的挂点（{@code Arms[0]} 右手 / {@code Arms[1]} 左手）与
     * {@code MultiModelHeldItemLayer} 的朝向约定渲染，布娃娃的手臂部位会带上女仆手里的东西。
     */
    private static void renderHeldItems(LittleMaidPartBlockEntity blockEntity, ModelMultiBase model, ModelRenderer anchor,
                                        PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        // 玩家骑在布娃娃上时不把玩家的手持物画到女仆手上
        if (!(blockEntity.getEntity() instanceof LivingEntity maid) || maid instanceof Player)
            return;
        var arms = model.Arms;
        if (arms == null || arms.length < 2)
            return;
        boolean mainRight = maid.getMainArm() == HumanoidArm.RIGHT;
        for (int i = 0; i < 2; i++) {
            ModelRenderer hand = arms[i];
            if (hand == null || !isHandAnchor(anchor, hand))
                continue;
            boolean left = i == 1;
            ItemStack stack = left
                    ? (mainRight ? maid.getOffhandItem() : maid.getMainHandItem())
                    : (mainRight ? maid.getMainHandItem() : maid.getOffhandItem());
            if (stack.isEmpty())
                continue;
            renderHandItem(maid, stack, left, hand, anchor, poseStack, bufferSource, light);
        }
    }

    private static boolean isHandAnchor(ModelRenderer anchor, ModelRenderer hand) {
        for (var current = hand; current != null; current = current.pearent) {
            if (current == anchor)
                return true;
        }
        return false;
    }

    private static void renderHandItem(LivingEntity maid, ItemStack stack, boolean left, ModelRenderer hand, ModelRenderer anchor,
                                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        float[] handPath = new float[3];
        restPath(hand, handPath);
        float[] basePath = new float[3];
        restPath(anchor, basePath);

        poseStack.pushPose();
        poseStack.translate(
                (handPath[0] - basePath[0]) / 16.0F,
                (handPath[1] - basePath[1]) / 16.0F,
                (handPath[2] - basePath[2]) / 16.0F
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(left ? -0.0125F : 0.0125F, 0.05F, -0.15F);
        ItemDisplayContext context = left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        Minecraft.getInstance().getItemRenderer().renderStatic(
                maid, stack, context, left, poseStack, bufferSource,
                maid.level(), light, OverlayTexture.NO_OVERLAY, maid.getId() + context.ordinal()
        );
        poseStack.popPose();
    }

    /** 解析定义文件里的 init 表情：按部件字段名隐藏/显示或调整静止姿势（例如闭眼）。 */
    private static Map<ModelRenderer, RagdollExpressions.Expression> resolveInitExpressions(
            LittleMaidPartBlockEntity blockEntity, LittleMaidPartRenderCache.Entry entry) {
        var defFile = DefFileLoader.getDefFile(blockEntity.getPartData().defFile());
        if (defFile == null)
            return Map.of();
        var result = new HashMap<ModelRenderer, RagdollExpressions.Expression>();
        defFile.expressions().getExpression("init").ifPresent(actions -> actions.forEach((name, expression) -> {
            var node = entry.node(name);
            if (node != null)
                result.put(node, expression);
        }));
        return result;
    }

    private static void renderNode(ModelRenderer node, ModelRenderer anchor, boolean flatChild, PoseStack poseStack,
                                   Map<ModelRenderer, RagdollExpressions.Expression> init) {
        var states = new ArrayList<NodeState>();
        prepare(node, init, states);

        float[] self = new float[3];
        restPath(node.pearent, self);
        float[] base = new float[3];
        restPath(anchor, base);

        poseStack.pushPose();
        poseStack.translate((self[0] - base[0]) / 16.0F, (self[1] - base[1]) / 16.0F, (self[2] - base[2]) / 16.0F);
        var children = node.childModels;
        if (flatChild)
            node.childModels = null;
        node.render(SCALE, true);
        if (flatChild)
            node.childModels = children;
        poseStack.popPose();

        states.forEach(NodeState::restore);
    }

    /** 渲染前把部件树归位到静止姿势，避免复用女仆渲染残留的动画状态。 */
    private static void prepare(ModelRenderer node, Map<ModelRenderer, RagdollExpressions.Expression> init, List<NodeState> states) {
        states.add(new NodeState(node,
                node.rotateAngleX, node.rotateAngleY, node.rotateAngleZ,
                node.offsetX, node.offsetY, node.offsetZ,
                node.showModel, node.isHidden, node.isRendering));
        node.rotateAngleX = 0.0F;
        node.rotateAngleY = 0.0F;
        node.rotateAngleZ = 0.0F;
        node.offsetX = 0.0F;
        node.offsetY = 0.0F;
        node.offsetZ = 0.0F;
        node.showModel = true;
        node.isHidden = false;
        node.isRendering = true;
        applyInit(node, init.get(node));
        if (node.childModels != null)
            node.childModels.forEach(child -> prepare(child, init, states));
    }

    private static void applyInit(ModelRenderer node, @Nullable RagdollExpressions.Expression expression) {
        if (expression == null)
            return;
        switch (expression.actionType()) {
            case "hide" -> node.showModel = false;
            case "show" -> node.showModel = true;
            default -> {
            }
        }
        node.offsetX = (float) expression.transform().x / 16.0F;
        node.offsetY = (float) expression.transform().y / 16.0F;
        node.offsetZ = (float) expression.transform().z / 16.0F;
        node.rotateAngleX = (float) Math.toRadians(expression.rotation().x);
        node.rotateAngleY = (float) Math.toRadians(expression.rotation().y);
        node.rotateAngleZ = (float) Math.toRadians(expression.rotation().z);
    }

    /** 模型空间中从 mainFrame 到指定节点的旋转点累加和。 */
    private static void restPath(@Nullable ModelRenderer node, float[] out) {
        for (var current = node; current != null; current = current.pearent) {
            out[0] += current.rotationPointX;
            out[1] += current.rotationPointY;
            out[2] += current.rotationPointZ;
        }
    }

    private static String modelNameOf(LittleMaidPartBlockEntity blockEntity) {
        var holder = LMTextureManager.INSTANCE.getTexture(blockEntity.getTextureName()).orElse(null);
        return holder == null ? blockEntity.getPartData().defFile().getPath() : holder.getModelName();
    }

    @Nullable
    private static ResourceLocation resolveTexture(LittleMaidPartBlockEntity blockEntity) {
        TextureHolder holder = LMTextureManager.INSTANCE.getTexture(blockEntity.getTextureName()).orElse(null);
        if (holder == null)
            return null;
        TextureColors color;
        try {
            color = TextureColors.getColor(blockEntity.getTextureColor());
        } catch (IllegalArgumentException e) {
            color = TextureColors.BROWN;
        }
        return holder.getTexture(color, blockEntity.isContract(), false).orElse(null);
    }

    private record NodeState(ModelRenderer node,
                             float rotateX, float rotateY, float rotateZ,
                             float offsetX, float offsetY, float offsetZ,
                             boolean showModel, boolean hidden, boolean rendering) {
        void restore() {
            node.rotateAngleX = rotateX;
            node.rotateAngleY = rotateY;
            node.rotateAngleZ = rotateZ;
            node.offsetX = offsetX;
            node.offsetY = offsetY;
            node.offsetZ = offsetZ;
            node.showModel = showModel;
            node.isHidden = hidden;
            node.isRendering = rendering;
        }
    }
}
