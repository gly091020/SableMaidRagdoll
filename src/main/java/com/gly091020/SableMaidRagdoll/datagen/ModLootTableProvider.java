package com.gly091020.SableMaidRagdoll.datagen;

import com.github.yimeng261.maidspell.compat.irons_spellbooks.registry.IronsSpellbooksCompatItems;
import com.github.yimeng261.maidspell.item.MaidSpellItems;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(ModAdvancementLootSubProvider::new, LootContextParamSets.ADVANCEMENT_REWARD)
        ), lookup);
    }

    private static class ModBlockLootSubProvider implements LootTableSubProvider {
        private ModBlockLootSubProvider(HolderLookup.Provider registries) {
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            dropSelf(consumer, TLMInitBlocks.MAID_PART_BLOCK.get());
            dropSelf(consumer, TLMInitBlocks.MAID_FAIRY_PART_BLOCK.get());
            dropSelf(consumer, InitBlocks.MAID_DOLL_BLOCK.get());
            dropSelf(consumer, InitBlocks.MOB_CANNON_BLOCK.get());
            consumer.accept(InitBlocks.TNT_CAKE_BLOCK.get().getLootTable(), new LootTable.Builder());
        }
    }

    private static class ModAdvancementLootSubProvider implements LootTableSubProvider{
        private ModAdvancementLootSubProvider(HolderLookup.Provider registries) {
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            consumer.accept(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID,
                    "cursh_wine_fox_boss_rewards")), new LootTable.Builder().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(MaidSpellItems.DREAM_CAT_CRYSTAL))
            ));
        }
    }

    private static void dropSelf(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer, Block block) {
        if (block.getLootTable() == BuiltInLootTables.EMPTY) {
            return;
        }
        LootTable.Builder builder = LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(block)));
        consumer.accept(block.getLootTable(), builder);
    }
}
