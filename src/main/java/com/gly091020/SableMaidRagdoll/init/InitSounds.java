package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, SableMaidRagdoll.MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> BROOM_MAN = SOUNDS.register("broom_man", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "broom_man")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERMELON_HURT = SOUNDS.register("watermelon_hurt", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "watermelon_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GCJ_SOUND = SOUNDS.register("gcj_sound", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "gcj_sound")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DOG_CALL = SOUNDS.register("dog_call", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "dog_call")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIG_DOG = SOUNDS.register("big_dog", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "big_dog")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DROP = SOUNDS.register("drop", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "drop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HUNGRY = SOUNDS.register("hungry", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "hungry")));
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE = SOUNDS.register("pipe", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "pipe")));

    public static void init(IEventBus bus){
        SOUNDS.register(bus);
    }
}
