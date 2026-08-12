package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class SableMaidRagdollDatagen {
    private SableMaidRagdollDatagen() {
    }

    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModSoundDefinitionsProvider(packOutput, helper));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, helper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, helper));

        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput, lookup));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookup));
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(packOutput, lookup, helper));
        ModTagsProvider.ModBlockTagsProvider blockTags = new ModTagsProvider.ModBlockTagsProvider(packOutput, lookup, helper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ModTagsProvider.ModItemTagsProvider(packOutput, lookup, blockTags.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new ModTagsProvider.ModDamageTypeTagsProvider(packOutput, lookup, helper));
    }
}
