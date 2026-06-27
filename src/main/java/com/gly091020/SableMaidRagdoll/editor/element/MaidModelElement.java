package com.gly091020.SableMaidRagdoll.editor.element;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.models.MaidModels;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaidModelElement extends UIElement {
    public final String modelID;
    @Nullable
    private final BedrockModel<?> model;
    @Nullable
    private final MaidModelInfo modelInfo;
    public MaidModelElement(String modelID){
        this.modelID = modelID;
        model = MaidModels.getInstance().getModel(modelID).orElse(null);
        modelInfo = MaidModels.getInstance().getInfo(modelID).orElse(null);
    }

    @Override
    public void drawContents(@NotNull GUIContext guiContext) {
        super.drawContents(guiContext);
        renderBedrock(guiContext);
        renderHover(guiContext);
        renderTip(guiContext);
    }

    private void renderTip(GUIContext context){
        if(modelInfo != null && isHover())
            context.graphics.renderTooltip(context.mc.font, getTooltip(), Optional.empty(), context.mouseX, context.mouseY);
    }

    private void renderHover(GUIContext context){
        if(isHover()){
            context.pose.pushPose();
            context.pose.translate(0, 0, 100);
            RenderSystem.enableBlend();
            context.graphics.fill(
                    (int) getContentX(),
                    (int) getContentY(),
                    (int) (getContentX() + getContentWidth()),
                    (int) (getContentY() + getContentHeight()),
                    0x55FFFFFF);
            RenderSystem.disableBlend();
            context.pose.popPose();
        }
    }

    private List<Component> getTooltip() {
        if (modelInfo == null) return List.of();

        var result = new ArrayList<Component>();

        result.add(Component.translatable(
                "text.sablemaidragdoll.model_view.tip1",
                MaidModelHelper.paste943String(modelInfo.getName())
        ));

        var descList = modelInfo.getDescription().stream()
                .map(s -> MaidModelHelper.paste943String(s).withStyle(ChatFormatting.GRAY))
                .toList();
        result.addAll(descList);
        result.add(Component.literal(modelID).withStyle(ChatFormatting.DARK_GRAY));
        return result;
    }

    private void renderBedrock(GUIContext guiContext){
        if(model == null || modelInfo == null)return;
        MaidModelHelper.resetModel(model);
        var vc = guiContext.graphics.bufferSource().getBuffer(model.renderType(modelInfo.getTexture()));
        guiContext.pose.pushPose();
        guiContext.pose.translate(getContentX() + getContentWidth() / 2, getContentY() + 10, 0);
        guiContext.pose.mulPose(Axis.YP.rotationDegrees(180));
        guiContext.pose.scale(30, 30, 30);
        model.renderToBuffer(guiContext.pose.pose,
                vc,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF);
        guiContext.pose.popPose();
    }
}
