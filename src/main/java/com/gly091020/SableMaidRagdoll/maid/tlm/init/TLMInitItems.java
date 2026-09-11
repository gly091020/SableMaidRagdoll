package com.gly091020.SableMaidRagdoll.maid.tlm.init;

import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableMaidRagdoll.item.spawn_egg.RagdollableMaidSpawnEgg;
import com.gly091020.SableMaidRagdoll.item.spawn_egg.RagdollableWineFoxSpawnEgg;
import com.gly091020.SableMaidRagdoll.item.spawn_egg.WineFoxSpawnEgg;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TLMInitItems {
    public static DeferredHolder<Item, WineFoxSpawnEgg> WINE_FOX_SPAWN_EGG;
    public static DeferredHolder<Item, RagdollableMaidSpawnEgg> RAGDOLLABLE_MAID_SPAWN_EGG;
    public static DeferredHolder<Item, RagdollableWineFoxSpawnEgg> RAGDOLLABLE_WINE_FOX_SPAWN_EGG;
    public static void init() {
        WINE_FOX_SPAWN_EGG = InitItems.ITEMS.register("winefox_spawn_egg",
                r -> new WineFoxSpawnEgg());

        RAGDOLLABLE_MAID_SPAWN_EGG = InitItems.ITEMS.register("ragdollanle_maid_spawn_egg",
                r -> new RagdollableMaidSpawnEgg());

        RAGDOLLABLE_WINE_FOX_SPAWN_EGG = InitItems.ITEMS.register("ragdollanle_wine_fox_spawn_egg",
                r -> new RagdollableWineFoxSpawnEgg());
    }
}