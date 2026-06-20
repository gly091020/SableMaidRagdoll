package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableMaidRagdoll.command.MaidRagdollCommand;
import com.gly091020.SableMaidRagdoll.server.GlobalHandledMaidPart;
import com.gly091020.SableMaidRagdoll.util.MaidPartColliderBoxManager;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = SableMaidRagdoll.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        MaidRagdollCommand.registry(dispatcher);
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event){
        MaidPartColliderBoxManager.reset();
        GlobalHandledMaidPart.clear();
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event){
        MaidPartDefFileLoader.init();
    }
}
