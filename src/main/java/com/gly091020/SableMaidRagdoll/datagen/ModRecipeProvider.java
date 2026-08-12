package com.gly091020.SableMaidRagdoll.datagen;

import com.github.tartaricacid.touhoulittlemaid.datagen.builder.AltarRecipeBuilder;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
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
    }

    private static ResourceLocation modId(String path) {
        return ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, path);
    }

    private static TagKey<net.minecraft.world.item.Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
