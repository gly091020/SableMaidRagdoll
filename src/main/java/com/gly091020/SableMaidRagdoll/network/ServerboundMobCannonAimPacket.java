package com.gly091020.SableMaidRagdoll.network;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundMobCannonAimPacket(BlockPos blockPos, double xRot, double yRot) implements CustomPacketPayload {
    public static final Type<ServerboundMobCannonAimPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "mob_cannon_aim"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundMobCannonAimPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerboundMobCannonAimPacket::blockPos,
                    ByteBufCodecs.DOUBLE,
                    ServerboundMobCannonAimPacket::xRot,
                    ByteBufCodecs.DOUBLE,
                    ServerboundMobCannonAimPacket::yRot,
                    ServerboundMobCannonAimPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            if (player.distanceToSqr(blockPos.getCenter()) > 64) return;
            if (!(level.getBlockEntity(blockPos) instanceof MobCannonBlockEntity blockEntity)) return;
            blockEntity.setXRot(Math.min(90, Math.max(-60, xRot)));
            blockEntity.setYRot(Mth.wrapDegrees(yRot));
        });
    }
}
