package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
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
        super(output, Set.of(), List.of(new SubProviderEntry(ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)), lookup);
    }

    private static class ModBlockLootSubProvider implements LootTableSubProvider {
        private ModBlockLootSubProvider(HolderLookup.Provider registries) {
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
            dropSelf(consumer, InitBlocks.MAID_PART_BLOCK.get());
            dropSelf(consumer, InitBlocks.MAID_FAIRY_PART_BLOCK.get());
            dropSelf(consumer, InitBlocks.MAID_DOLL_BLOCK.get());
            consumer.accept(InitBlocks.TNT_CAKE_BLOCK.get().getLootTable(), new LootTable.Builder());
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
