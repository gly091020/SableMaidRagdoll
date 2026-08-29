package com.gly091020.SableMaidRagdoll.datagen;

import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.init.InitTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
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
                    .add(InitBlocks.MAID_PART_BLOCK.get());
        }
    }

    public static class ModItemTagsProvider extends ItemTagsProvider {
        public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagLookup<net.minecraft.world.level.block.Block>> blockTags, ExistingFileHelper helper) {
            super(output, lookup, blockTags, SableMaidRagdoll.MODID, helper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(InitTags.MAID_TO_RAGDOLL_TAG)
                    .add(Items.MACE)
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("beds")))
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("anvil")))
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("doors")))
                    .addTag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("shovels")))
                    .add(InitItems.MAID_BEACON.get())
                    .add(InitItems.BOOKSHELF.get())
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
            tag(InitTags.ALWAYS_TO_RAGDOLL_TAG)
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
                    .add(DamageTypes.WITHER_SKULL)
                    .addOptional(ResourceLocation.parse("superbwarfare:vehicle_strike"));

            tag(InitTags.LAOWU_HURT_DANCE)
                    .add(DamageTypes.FLY_INTO_WALL)
                    .addOptional(ResourceLocation.parse("superbwarfare:vehicle_strike"));
        }
    }
}
