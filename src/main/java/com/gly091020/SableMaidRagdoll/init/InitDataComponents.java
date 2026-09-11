package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SableMaidRagdoll.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MaidDollData>> MAID_DOLL_DATA = DATA_COMPONENTS.register("maid_doll_data", r ->
            DataComponentType.<MaidDollData>builder().persistent(MaidDollData.CODEC).networkSynchronized(MaidDollData.STREAM_CODEC).build()
    );

    public static void init(IEventBus bus){
        DATA_COMPONENTS.register(bus);
    }
}
