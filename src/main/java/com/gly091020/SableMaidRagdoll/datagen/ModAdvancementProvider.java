package com.gly091020.SableMaidRagdoll.datagen;

import com.github.tartaricacid.touhoulittlemaid.advancements.maid.MaidEventTrigger;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitCustomStats;
import com.gly091020.SableMaidRagdoll.util.MaidCreativeTab;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollAdvancementEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {
    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        super(output, lookup, helper, List.of(new ModAdvancements()));
    }

    private static class ModAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(
                            com.gly091020.SableMaidRagdoll.init.InitItems.MOD_ICON_ITEM.get(),
                            Component.translatable("advancements.sablemaidragdoll.root.title"),
                            Component.translatable("advancements.sablemaidragdoll.root.description"),
                            ResourceLocation.withDefaultNamespace("textures/block/light_blue_wool.png"),
                            AdvancementType.TASK,
                            false,
                            false,
                            false
                    )
                    .addCriterion("enter_world", PlayerTrigger.TriggerInstance.tick())
                    .save(saver, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "root"), existingFileHelper);

            var cheatDeath = itemObtained(saver, existingFileHelper, root, "cheat_death_bauble", com.gly091020.SableMaidRagdoll.init.InitItems.CHEAT_DEATH_BAUBLE_ITEM.get());
            itemObtained(saver, existingFileHelper, cheatDeath, "maid_mace", com.gly091020.SableMaidRagdoll.init.InitItems.MAID_MACE_ITEM.get());
            var maidDoll = itemObtained(saver, existingFileHelper, cheatDeath, "player_cheat_death", MaidCreativeTab.getDollDisplayStack(), AdvancementType.GOAL);
            var mobCannon = itemObtained(saver, existingFileHelper, cheatDeath, "mob_cannon", com.gly091020.SableMaidRagdoll.init.InitItems.MOB_CANNON_ITEM.get());

            maidEvent(saver, existingFileHelper, root, "hit_maid_ragdoll", MaidRagdollAdvancementEvents.HIT_MAID.getName(), InitItems.FAVORABILITY_TOOL_REDUCE);
            maidEvent(saver, existingFileHelper, maidDoll, "control_maid", MaidRagdollAdvancementEvents.CONTROL_MAID.getName(), InitItems.GARAGE_KIT);
            Advancement.Builder.advancement()
                    .parent(mobCannon)
                    .display(
                            com.gly091020.SableMaidRagdoll.init.InitItems.MOB_CANNON_ITEM.get(),
                            Component.translatable("advancements.sablemaidragdoll.double_cannon.title"),
                            Component.translatable("advancements.sablemaidragdoll.double_cannon.description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            true
                    )
                    .addCriterion("double_cannon", MaidEventTrigger.create(MaidRagdollAdvancementEvents.DOUBLE_CANNON.getName()))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "double_cannon"), existingFileHelper);

            statMilestone(saver, existingFileHelper, root, "maid_knocked_away_100", com.gly091020.SableMaidRagdoll.init.InitItems.MAID_MACE_ITEM.get(),
                    InitCustomStats.MAID_KNOCKED_AWAY.get(), 100);
            statMilestone(saver, existingFileHelper, maidDoll, "to_maid_100", com.gly091020.SableMaidRagdoll.init.InitItems.PLAYER_CHEAT_DEATH_ITEM.get(),
                    InitCustomStats.TO_MAID.get(), 100);
        }

        private static AdvancementHolder itemObtained(Consumer<AdvancementHolder> saver, ExistingFileHelper helper, AdvancementHolder parent, String id, ItemLike item) {
            return Advancement.Builder.advancement()
                    .parent(parent)
                    .display(
                            item,
                            Component.translatable("advancements.sablemaidragdoll." + id + ".title"),
                            Component.translatable("advancements.sablemaidragdoll." + id + ".description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(item))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, id), helper);
        }

        private static AdvancementHolder itemObtained(Consumer<AdvancementHolder> saver, ExistingFileHelper helper, AdvancementHolder parent, String id, ItemStack item, AdvancementType type) {
            return Advancement.Builder.advancement()
                    .parent(parent)
                    .display(
                            item,
                            Component.translatable("advancements.sablemaidragdoll." + id + ".title"),
                            Component.translatable("advancements.sablemaidragdoll." + id + ".description"),
                            null,
                            type,
                            true,
                            true,
                            false
                    )
                    .addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(item.getItem()))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, id), helper);
        }

        private static AdvancementHolder maidEvent(Consumer<AdvancementHolder> saver, ExistingFileHelper helper, AdvancementHolder parent, String id, String event, ItemLike icon) {
            return Advancement.Builder.advancement()
                    .parent(parent)
                    .display(
                            icon,
                            Component.translatable("advancements.sablemaidragdoll." + id + ".title"),
                            Component.translatable("advancements.sablemaidragdoll." + id + ".description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            true,
                            false
                    )
                    .addCriterion("maid_event", MaidEventTrigger.create(event))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, id), helper);
        }

        private static AdvancementHolder statMilestone(Consumer<AdvancementHolder> saver, ExistingFileHelper helper, AdvancementHolder parent,
                                                       String id, ItemLike icon, ResourceLocation stat, int value) {
            Holder.Reference<ResourceLocation> statHolder = BuiltInRegistries.CUSTOM_STAT.getHolderOrThrow(
                    ResourceKey.create(Registries.CUSTOM_STAT, stat));
            return Advancement.Builder.advancement()
                    .parent(parent)
                    .display(
                            icon,
                            Component.translatable("advancements.sablemaidragdoll." + id + ".title"),
                            Component.translatable("advancements.sablemaidragdoll." + id + ".description"),
                            null,
                            AdvancementType.CHALLENGE,
                            true,
                            true,
                            true
                    )
                    .addCriterion("stat_reached", PlayerTrigger.TriggerInstance.located(
                            EntityPredicate.Builder.entity()
                                    .subPredicate(PlayerPredicate.Builder.player()
                                            .addStat(Stats.CUSTOM, statHolder, MinMaxBounds.Ints.atLeast(value))
                                            .build())))
                    .save(saver, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, id), helper);
        }
    }
}
