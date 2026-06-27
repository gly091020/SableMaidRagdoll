package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.command.MaidRagdollClientCommand;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = SableMaidRagdoll.MODID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(
                SableMaidRagdoll.MAID_PART_BLOCK_ENTITY.get(),
                (context) -> new MaidPartRenderer()
        );
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        MaidRagdollClientCommand.registry(event.getDispatcher());
    }
}
