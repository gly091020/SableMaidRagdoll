package com.gly091020.SableMaidRagdoll.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static com.gly091020.SableMaidRagdoll.init.InitBlockEntities.MOB_CANNON_BLOCK_ENTITY;

public class InitCapabilities {
    public static void init(IEventBus bus) {
        bus.addListener(InitCapabilities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MOB_CANNON_BLOCK_ENTITY.get(), (blockEntity, side) -> blockEntity);
    }
}
