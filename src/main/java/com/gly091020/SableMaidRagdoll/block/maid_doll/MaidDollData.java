package com.gly091020.SableMaidRagdoll.block.maid_doll;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MaidDollData(String ragdollType, String modelID, String soundID) {
    public static final Codec<MaidDollData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("ragdollType").forGetter(MaidDollData::ragdollType),
            Codec.STRING.fieldOf("modelID").forGetter(MaidDollData::modelID),
            Codec.STRING.fieldOf("soundID").forGetter(MaidDollData::soundID)
    ).apply(i, MaidDollData::new));
    public static final StreamCodec<ByteBuf, MaidDollData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    public static final MaidDollData EMPTY = new MaidDollData("", "", "");
}
