package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableMaidRagdoll.command.MaidRagdollCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SableMaidRagdoll.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        MaidRagdollCommand.registry(dispatcher);
    }
}
