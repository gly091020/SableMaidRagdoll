package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, SableMaidRagdoll.MODID, helper);
    }

    @Override
    public void registerSounds() {
        add(InitSounds.PIPE.get(), definition()
                .subtitle("sound.sablemaidragdoll.pipe")
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "pipe"))));
        add(InitSounds.HUNGRY.get(), definition()
                .subtitle("sound.sablemaidragdoll.hungry")
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "hungry"))));
        add(InitSounds.DROP.get(), definition()
                .subtitle("sound.sablemaidragdoll.drop")
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "drop"))));
        add(InitSounds.BIG_DOG.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "big_dog"))));
        add(InitSounds.DOG_CALL.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "dog_call"))));
        add(InitSounds.GCJ_SOUND.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "gcj_sound"))));
        add(InitSounds.WATERMELON_HURT.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "watermelon_hurt"))));
        add(InitSounds.BROOM_MAN.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "broom_man"))));
        add(InitSounds.CHICKEN.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "chicken"))));
        add(InitSounds.CANNON_LAUNCH.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "cannon_launch"))));
    }
}
