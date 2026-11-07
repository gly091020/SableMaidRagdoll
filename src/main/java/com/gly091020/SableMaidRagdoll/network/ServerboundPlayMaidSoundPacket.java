package com.gly091020.SableMaidRagdoll.network;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableMaidRagdoll.maid.api.MaidSoundType;
import com.gly091020.SableRagdollLib.api.RagdollManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundPlayMaidSoundPacket() implements CustomPacketPayload {
    public static final Type<ServerboundPlayMaidSoundPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "play_maid_sound"));
    public static final StreamCodec<ByteBuf, ServerboundPlayMaidSoundPacket> STREAM_CODEC = StreamCodec.unit(new ServerboundPlayMaidSoundPacket());

    public ServerboundPlayMaidSoundPacket() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context){
        context.enqueueWork(() -> {
            var ragdoll = RagdollManager.get(context.player());
            if(ragdoll == null)return;
            if(!ragdoll.getExtraData().contains("PCDI_soundID", Tag.TAG_STRING) || !ragdoll.getExtraData().contains("PCDI_typeID", Tag.TAG_STRING))return;
            var soundID = ragdoll.getExtraData().getString("PCDI_soundID");
            var typeID = ragdoll.getExtraData().getString("PCDI_typeID");
            var r = MaidRagdollTypesManager.getType(typeID);
            if(r == null)return;
            r.playMaidSound(context.player().level(), context.player().position(), soundID, MaidSoundType.IDLE, 1);
        });
    }
}
