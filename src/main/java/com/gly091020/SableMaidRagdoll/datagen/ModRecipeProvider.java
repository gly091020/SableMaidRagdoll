package com.gly091020.SableMaidRagdoll.datagen;

import com.github.tartaricacid.touhoulittlemaid.datagen.builder.AltarRecipeBuilder;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        AltarRecipeBuilder.shapeless(RecipeCategory.MISC, SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get())
                .requires(Items.YELLOW_DYE)
                .requires(ItemTags.WOOL)
                .save(recipeOutput, modId("cheat_death_bauble"));

        AltarRecipeBuilder.shapeless(RecipeCategory.MISC, SableMaidRagdoll.MAID_MACE_ITEM.get())
                .power(1.0F)
                .requires(SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get())
                .requires(3, commonTag("storage_blocks/iron"))
                .save(recipeOutput, modId("maid_mace"));

        AltarRecipeBuilder.shapeless(RecipeCategory.MISC, SableMaidRagdoll.PLAYER_CHEAT_DEATH_ITEM.get())
                .power(1.0F)
                .requires(SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get())
                .requires(commonTag("gems/diamond"))
                .save(recipeOutput, modId("player_cheat_death"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get())
                .requires(Items.YELLOW_DYE)
                .requires(ItemTags.WOOL)
                .requires(commonTag("ingots/iron"))
                .unlockedBy("get", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
                .save(recipeOutput, modId("cheat_death_bauble1"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SableMaidRagdoll.SONIC_WAVE_ITEM.get())
                .requires(SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get())
                .requires(Items.ECHO_SHARD)
                .unlockedBy("get", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ECHO_SHARD))
                .save(recipeOutput, modId("sonic_wave_item"));
    }

    private static ResourceLocation modId(String path) {
        return ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, path);
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
