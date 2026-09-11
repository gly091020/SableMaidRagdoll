package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.advancements.MaidRagdollEventTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitTrigger {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, SableMaidRagdoll.MODID);
    public static final DeferredHolder<CriterionTrigger<?>, MaidRagdollEventTrigger> EVENT_TRIGGER = TRIGGERS.register("ragdoll_event", MaidRagdollEventTrigger::new);

    public static void init(IEventBus bus){
        TRIGGERS.register(bus);
    }
}
