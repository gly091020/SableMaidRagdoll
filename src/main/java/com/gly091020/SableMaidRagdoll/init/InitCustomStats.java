package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitCustomStats {
    private static final DeferredRegister<ResourceLocation> CUSTOM_STAT = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT, SableMaidRagdoll.MODID);
    public static DeferredHolder<ResourceLocation, ResourceLocation> TO_MAID = CUSTOM_STAT.register("to_maid", r -> r);
    public static DeferredHolder<ResourceLocation, ResourceLocation> MAID_KNOCKED_AWAY = CUSTOM_STAT.register("maid_knocked_away", r -> r);

    public static void init(IEventBus bus){
        CUSTOM_STAT.register(bus);
    }
}
