package com.gly091020.SableMaidRagdoll.network;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MixinFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundBroomManPacket(int entityID, Vec3 vec3) implements CustomPacketPayload{
    public static final CustomPacketPayload.Type<ServerboundBroomManPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "broom_man"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundBroomManPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ServerboundBroomManPacket::entityID,
                    ByteBufCodecs.fromCodec(Vec3.CODEC),
                    ServerboundBroomManPacket::vec3,
                    ServerboundBroomManPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var entity = context.player().level().getEntity(entityID);
            if(entity instanceof EntityBroom broom)
                MixinFunction.broomMan(broom, vec3);
        });
    }
}
