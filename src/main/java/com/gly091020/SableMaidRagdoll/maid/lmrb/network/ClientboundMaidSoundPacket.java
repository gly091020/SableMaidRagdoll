package com.gly091020.SableMaidRagdoll.maid.lmrb.network;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.maid.lmrb.client.LMRBSoundPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 -> 客户端：在指定坐标播放声音包里的音效。
 * <p>
 * 与 LMML 的 {@code LMSoundPacket} 不同，这里不携带实体 id，
 * 因此女仆消失后（布娃娃状态）依然可以播放 idle / hurt 语音。
 * 音效文件由客户端用自己的声音包解析。
 */
public record ClientboundMaidSoundPacket(String soundPack, String soundName, Vec3 pos, float volume)
        implements CustomPacketPayload {

    public static final Type<ClientboundMaidSoundPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "maid_sound"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundMaidSoundPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ClientboundMaidSoundPacket::soundPack,
                    ByteBufCodecs.STRING_UTF8, ClientboundMaidSoundPacket::soundName,
                    ByteBufCodecs.DOUBLE, packet -> packet.pos().x,
                    ByteBufCodecs.DOUBLE, packet -> packet.pos().y,
                    ByteBufCodecs.DOUBLE, packet -> packet.pos().z,
                    ByteBufCodecs.FLOAT, ClientboundMaidSoundPacket::volume,
                    (soundPack, soundName, x, y, z, volume) ->
                            new ClientboundMaidSoundPacket(soundPack, soundName, new Vec3(x, y, z), volume)
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> LMRBSoundPlayer.play(soundPack, soundName, pos, volume));
    }
}
