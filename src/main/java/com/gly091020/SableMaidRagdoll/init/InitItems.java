package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.gly091020.SableMaidRagdoll.init.InitBlocks.MOB_CANNON_BLOCK;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SableMaidRagdoll.MODID);
    public static final DeferredHolder<Item, BlockItem> TNT_CAKE_ITEM = ITEMS.register("tnt_cake", r -> new BlockItem(InitBlocks.TNT_CAKE_BLOCK.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, SonicWaveItem> SONIC_WAVE_ITEM = ITEMS.register("sonic_wave", resourceLocation -> new SonicWaveItem());
    public static final DeferredHolder<Item, MaidMaceItem> MAID_MACE_ITEM = ITEMS.register("maid_mace", resourceLocation -> new MaidMaceItem());
    public static final DeferredHolder<Item, CopyRagdollIDItem> COPY_RAGDOLL_ID_ITEM = ITEMS.register("copy_ragdoll_id", resourceLocation -> new CopyRagdollIDItem());
    public static final DeferredHolder<Item, PlayerCheatDeathItem> PLAYER_CHEAT_DEATH_ITEM = ITEMS.register("player_cheat_death", resourceLocation -> new PlayerCheatDeathItem());
    public static final DeferredHolder<Item, RagdollWandItem> RAGDOLL_WAND_ITEM = ITEMS.register("ragdoll_wand", resourceLocation -> new RagdollWandItem());
    public static final DeferredHolder<Item, CheatDeathBaubleItem> CHEAT_DEATH_BAUBLE_ITEM = ITEMS.register("cheat_death_bauble", resourceLocation -> new CheatDeathBaubleItem());
    public static final DeferredHolder<Item, BlockItem> MOB_CANNON_ITEM = ITEMS.register("mob_cannon", resourceLocation -> new BlockItem(MOB_CANNON_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MOD_ICON_ITEM = ITEMS.register("mod_icon", resourceLocation -> new Item(new Item.Properties()));

    public static void init(IEventBus bus){
        ITEMS.register(bus);
    }
}
