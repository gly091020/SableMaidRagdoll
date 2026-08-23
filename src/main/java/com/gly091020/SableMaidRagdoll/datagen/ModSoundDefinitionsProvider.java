package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
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
        add(SableMaidRagdoll.PIPE.get(), definition()
                .subtitle("sound.sablemaidragdoll.pipe")
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "pipe"))));
        add(SableMaidRagdoll.HUNGRY.get(), definition()
                .subtitle("sound.sablemaidragdoll.hungry")
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "hungry"))));
        add(SableMaidRagdoll.DROP.get(), definition()
                .subtitle("sound.sablemaidragdoll.drop")
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "drop"))));
        add(SableMaidRagdoll.BIG_DOG.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "big_dog"))));
        add(SableMaidRagdoll.DOG_CALL.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "dog_call"))));
        add(SableMaidRagdoll.GCJ_SOUND.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "gcj_sound"))));
        add(SableMaidRagdoll.WATERMELON_HURT.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "watermelon_hurt"))));
        add(SableMaidRagdoll.BROOM_MAN.get(), definition()
                .with(sound(ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "broom_man"))));
    }
}
