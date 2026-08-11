package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.touhoulittlemaid.client.resource.listener.EmojiReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EmojiReloadListener.class)
public interface EmojiReloadListenerMixin {
    @Accessor("EMOJI_RESOURCES")
    static List<EmojiReloadListener.EmojiResource> getEmojis(){
        throw new AssertionError();
    };
}
