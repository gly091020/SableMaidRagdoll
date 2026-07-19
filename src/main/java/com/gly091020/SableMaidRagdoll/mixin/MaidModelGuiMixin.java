package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.model.MaidModelGui;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MaidModelGui.class)
public class MaidModelGuiMixin {
    @Inject(method = "addModelCustomTips(Lcom/github/tartaricacid/touhoulittlemaid/client/resource/pojo/MaidModelInfo;Ljava/util/List;)V", at = @At("RETURN"))
    public void addCanRagdoll(MaidModelInfo modelItem, List<Component> tooltips, CallbackInfo ci){
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, modelItem.getModelId().toString().replace(":", "/"));
        if (DefFileLoader.getDefFile(id) == null)return;
        tooltips.add(Component.translatable("text.sablemaidragdoll.supports_ragdoll"));
        if(Minecraft.getInstance().options.advancedItemTooltips)
            tooltips.add(Component.translatable("text.sablemaidragdoll.ragdoll_id", id.toString()));
    }

    @Inject(method = "drawRightEntity(Lnet/minecraft/client/gui/GuiGraphics;IILcom/github/tartaricacid/touhoulittlemaid/client/resource/pojo/MaidModelInfo;)V", at = @At("HEAD"))
    public void addBackground(GuiGraphics graphics, int posX, int posY, MaidModelInfo modelItem, CallbackInfo ci){
        if(!Minecraft.getInstance().options.advancedItemTooltips)return;
        var id = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, modelItem.getModelId().toString().replace(":", "/"));
        if (DefFileLoader.getDefFile(id) == null)return;
        int textureSize = 24;
        var x = posX - textureSize / 2;
        var y = posY - textureSize;

        RenderSystem.enableBlend();
        graphics.fill(x, y, x + textureSize, y + textureSize, 0X5000FF00);
        RenderSystem.disableBlend();
    }
}
