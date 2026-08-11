package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.chatbubble.EntityGraphics;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.texture.GifTexture;
import com.github.tartaricacid.touhoulittlemaid.client.resource.listener.EmojiReloadListener;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.IChatBubbleData;
import com.gly091020.SableMaidRagdoll.mixin.EmojiReloadListenerMixin;
import com.gly091020.SableMaidRagdoll.mixin.TextureManagerMixin;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RagdollChatBubbleRenderer {
    public static void render(EmojiReloadListener.EmojiResource resource, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTicks){
        var graphics = new EntityGraphics(bufferSource, poseStack, null, light, partialTicks);
        if(resource.isGif())registerGifImage(resource.location());
        renderBG(graphics, resource);
        graphics.blit(resource.location(), 0, 0, 0, 0, resource.width(), resource.height(), resource.width(), resource.height());
    }

    private static void renderBG(EntityGraphics graphics, EmojiReloadListener.EmojiResource resource){
        int offset = 5;

        int width = resource.width();
        int height = resource.height();
        ResourceLocation texture = IChatBubbleData.TYPE_2;
        int bgWidth = width + 2 * offset;
        int bgHeight = height + 2 * offset;

        graphics.getPoseStack().mulPose(Axis.YP.rotationDegrees(180));
        graphics.getPoseStack().scale(-0.025F, -0.025F, 0.025F);

        graphics.blitNineSliced(texture, -bgWidth / 2, -bgHeight, bgWidth, bgHeight, 8, 8, 48, 24, 0, 0);
        graphics.getPoseStack().translate(0, 0, -0.01);
        graphics.blit(texture, -8, -8, 16, 24, 16, 16);
        graphics.getPoseStack().translate(-bgWidth / 2d + offset, -bgHeight + offset, -0.01);
    }

    public static List<EmojiReloadListener.EmojiResource> getEmojis(){
        return EmojiReloadListenerMixin.getEmojis();
    }

    public static void registerGifImage(ResourceLocation emoji) {
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        if (((TextureManagerMixin)manager).getByPath().containsKey(emoji)) {
            return;
        }
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> manager.register(emoji, new GifTexture(emoji)));
        } else {
            manager.register(emoji, new GifTexture(emoji));
        }
    }
}
