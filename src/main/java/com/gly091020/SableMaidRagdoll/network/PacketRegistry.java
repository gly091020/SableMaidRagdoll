package com.gly091020.SableMaidRagdoll.network;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PacketRegistry {
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(SableMaidRagdoll.MODID).versioned("2");
        registrar.playToServer(
                ServerboundEmojiSelectPacket.TYPE,
                ServerboundEmojiSelectPacket.STREAM_CODEC,
                ServerboundEmojiSelectPacket::handle
        );

        registrar.playToServer(
                ServerboundBroomManPacket.TYPE,
                ServerboundBroomManPacket.STREAM_CODEC,
                ServerboundBroomManPacket::handle
        );
    }
}
