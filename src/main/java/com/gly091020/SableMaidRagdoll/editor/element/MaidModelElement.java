package com.gly091020.SableMaidRagdoll.editor.element;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.models.MaidModels;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaidModelElement extends UIElement {
    public final String modelID;
    @Nullable
    private final BedrockModel<?> model;
    @Nullable
    private final ResourceLocation texture;
    public MaidModelElement(String modelID){
        this.modelID = modelID;
        model = MaidModels.getInstance().getModel(modelID).orElse(null);
        var info = MaidModels.getInstance().getInfo(modelID);
        texture = info.map(MaidModelInfo::getTexture).orElse(null);
    }

    @Override
    public void drawContents(@NotNull GUIContext guiContext) {
        super.drawContents(guiContext);
        if(model == null || texture == null)return;
        MaidModelHelper.resetModel(model);
        var vc = guiContext.graphics.bufferSource().getBuffer(model.renderType(texture));
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
