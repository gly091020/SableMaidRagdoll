package com.gly091020.SableMaidRagdoll.advancements;

import com.gly091020.SableMaidRagdoll.init.InitTrigger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class MaidRagdollEventTrigger extends SimpleCriterionTrigger<MaidRagdollEventTrigger.Instance>  {
    public static Criterion<MaidRagdollEventTrigger.Instance> create(String eventName) {
        return InitTrigger.EVENT_TRIGGER.get().createCriterion(new MaidRagdollEventTrigger.Instance(Optional.empty(), eventName));
    }

    public void trigger(ServerPlayer serverPlayer, String eventName) {
        super.trigger(serverPlayer, instance -> instance.matches(eventName));
    }

    @Override
    public Codec<MaidRagdollEventTrigger.Instance> codec() {
        return MaidRagdollEventTrigger.Instance.CODEC;
    }

    public record Instance(Optional<ContextAwarePredicate> player, String eventName) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<MaidRagdollEventTrigger.Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MaidRagdollEventTrigger.Instance::player),
                        Codec.STRING.fieldOf("event").forGetter(MaidRagdollEventTrigger.Instance::eventName))
                .apply(instance, MaidRagdollEventTrigger.Instance::new));

        public boolean matches(String eventNameIn) {
            return this.eventName.equals(eventNameIn);
        }
    }
}
