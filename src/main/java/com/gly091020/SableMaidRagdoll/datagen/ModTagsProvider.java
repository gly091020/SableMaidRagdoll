package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModTagsProvider {
    private ModTagsProvider() {
    }

    public static class ModBlockTagsProvider extends BlockTagsProvider {
        public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
            super(output, lookup, SableMaidRagdoll.MODID, helper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sableragdolllib", "ragdoll_part")))
                    .add(SableMaidRagdoll.MAID_PART_BLOCK.get());
        }
    }

    public static class ModItemTagsProvider extends ItemTagsProvider {
        public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagLookup<net.minecraft.world.level.block.Block>> blockTags, ExistingFileHelper helper) {
            super(output, lookup, blockTags, SableMaidRagdoll.MODID, helper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "maid_to_ragdoll")))
                    .add(Items.MACE)
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("beds")))
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("anvil")))
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("doors")))
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("shovels")))
                    .add(BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("touhou_little_maid", "maid_beacon")).orElseThrow())
                    .addOptionalTag(ResourceLocation.fromNamespaceAndPath("superbwarfare", "hammer"))
                    .addOptionalTag(ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("iammusicplayer", "boombox"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createtreadmill", "treadmill"));
        }
    }

    public static class ModDamageTypeTagsProvider extends DamageTypeTagsProvider {
        public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
            super(output, lookup, SableMaidRagdoll.MODID, helper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "always_to_ragdoll")))
                    .add(DamageTypes.BAD_RESPAWN_POINT)
                    .add(DamageTypes.EXPLOSION)
                    .add(DamageTypes.FALL)
                    .add(DamageTypes.FALLING_ANVIL)
                    .add(DamageTypes.FLY_INTO_WALL)
                    .add(DamageTypes.PLAYER_EXPLOSION)
                    .add(DamageTypes.SONIC_BOOM)
                    .add(DamageTypes.SPIT)
                    .add(DamageTypes.STALAGMITE)
                    .add(DamageTypes.THROWN)
                    .add(DamageTypes.UNATTRIBUTED_FIREBALL)
                    .add(DamageTypes.WITHER_SKULL);
        }
    }
}
