package com.gly091020.SableMaidRagdoll.network;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.RagdollEmoji;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 -> 服务端：玩家在表情选择界面里选定（或清除）当前表情。
 * {@code emoji} 为表情贴图的 ResourceLocation 字符串；空字符串表示清除。
 */
public record ServerboundEmojiSelectPacket(ResourceLocation emoji) implements CustomPacketPayload {

    public static final Type<ServerboundEmojiSelectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "emoji_select"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundEmojiSelectPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ServerboundEmojiSelectPacket::emoji,
                    ServerboundEmojiSelectPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            // 只有正在乘坐女仆类型布娃娃的玩家才能修改表情
            if (!RagdollEmoji.isRagdollOfType(player, SableMaidRagdoll.RAGDOLL_TYPE)) return;

            if (emoji.equals(SableMaidRagdoll.EMPTY_EMOJI)) {
                RagdollEmoji.setEmoji(player, null);
            } else {
                RagdollEmoji.setEmoji(player, emoji);
            }
        });
    }
}
