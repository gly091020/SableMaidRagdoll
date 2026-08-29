package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SableMaidRagdoll.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ENABLE_CONTROL = DATA_COMPONENTS.register("enable_control", r ->
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MAID_SOUND = DATA_COMPONENTS.register("maid_sound", r ->
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MAID_MODEL = DATA_COMPONENTS.register("maid_model", r ->
       DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
    );

    public static void init(IEventBus bus){
        DATA_COMPONENTS.register(bus);
    }
}
