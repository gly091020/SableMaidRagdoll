package com.gly091020.SableMaidRagdoll.editor.sceneObject;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.resource.models.MaidModels;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.gly091020.SableMaidRagdoll.mixin.model.BedrockPartMixin;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.lowdragmc.lowdraglib2.editor.ui.sceneeditor.sceneobject.ISceneInteractable;
import com.lowdragmc.lowdraglib2.editor.ui.sceneeditor.sceneobject.ISceneRendering;
import com.lowdragmc.lowdraglib2.editor.ui.sceneeditor.sceneobject.SceneObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MaidPartObject extends SceneObject implements ISceneInteractable, ISceneRendering {
    private static final int MAX_LIGHT_TEXTURE = LightTexture.pack(15, 15);
    @Nullable
    public final String modelName;
    @Nullable
    public final String partName;
    public final VoxelShape shape;

    @Nullable
    private BedrockPart bedrockPart;
    @Nullable
    private MaidModelInfo modelInfo;
    @Nullable
    private RenderType renderType;
    public MaidPartObject(@Nullable String modelName, @Nullable String partName, @Nullable VoxelShape shape) {
        this.modelName = modelName;
        this.partName = partName;
        this.shape = shape == null? Shapes.empty() : shape;

        if(modelName == null || partName == null)return;

        var modelInfo = MaidModels.getInstance().getInfo(modelName);
        if(modelInfo.isEmpty())return;
        this.modelInfo = modelInfo.get();
        initBedrock();
    }

    private void initBedrock(){
        if(modelInfo == null || modelName == null)return;
        var model = MaidModels.getInstance().getModel(modelName);
        if(model.isEmpty())return;
        var map = model.get().getModelMap();
        bedrockPart = map.get(partName);
        renderType = model.get().renderType(modelInfo.getTexture());
    }

    @Override
    public void drawInternal(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        drawBedrock(poseStack, bufferSource);
    }

    public void drawBedrock(PoseStack poseStack, MultiBufferSource bufferSource){
        if(bedrockPart == null || modelInfo == null || renderType == null)return;
        MaidModelHelper.resetModel(bedrockPart);
        poseStack.pushPose();
        render(poseStack, bufferSource.getBuffer(renderType), bedrockPart);
        poseStack.popPose();
    }

    // 大家都是文明人，不要说出f开头那个词
    private void render(PoseStack poseStack, VertexConsumer consumer, BedrockPart part){
        if (part.visible) {
            boolean xNearZero = -1E-5F < part.xScale && part.xScale < 1E-5F;
            boolean yNearZero = -1E-5F < part.yScale && part.yScale < 1E-5F;
            boolean zNearZero = -1E-5F < part.zScale && part.zScale < 1E-5F;
            if ((xNearZero && yNearZero) || (xNearZero && zNearZero) || (yNearZero && zNearZero)) {
                return;
            }

            for (BedrockPart child : part.children) {
                if(!child.children.isEmpty())continue;
                render(poseStack, consumer, child);
            }

            if (!part.cubes.isEmpty() || !part.children.isEmpty()) {
                poseStack.pushPose();
                translateAndRotateAndScale(poseStack, part);
                ((BedrockPartMixin)part).invokeCompile(poseStack.last(), consumer, MAX_LIGHT_TEXTURE, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
                poseStack.popPose();
            }
        }
    }

    private void translateAndRotateAndScale(PoseStack poseStack, BedrockPart bedrockPart){
        if(bedrockPart.parent != null)translateAndRotateAndScale(poseStack, bedrockPart.parent);
        bedrockPart.translateAndRotateAndScale(poseStack);
    }

    @Override
    public VoxelShape getCollisionShape() {
        return shape;
    }
}
