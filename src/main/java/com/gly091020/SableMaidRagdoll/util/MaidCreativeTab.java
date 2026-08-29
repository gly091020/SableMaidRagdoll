package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.DefaultMaidSoundPack;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitCreativeModeTab;
import com.gly091020.SableMaidRagdoll.init.InitDataComponents;
import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MaidCreativeTab {
    public static ItemStack getDollDisplayStack(){
        var stack = new ItemStack(InitItems.PLAYER_CHEAT_DEATH_ITEM.get(), 1);
        stack.set(InitDataComponents.MAID_MODEL, "authors_and_credits:wine_fox_taisho");
        return stack;
    }

    public static void getAllMainItem(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output){
        if(FMLEnvironment.dist.isDedicatedServer())return;
        if(SableMaidRagdoll.CONFIG.items.playerCheatDeathItem)
            output.accept(InitItems.PLAYER_CHEAT_DEATH_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.cheatDeathBauble)
            output.accept(InitItems.CHEAT_DEATH_BAUBLE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.maidMace)
            output.accept(InitItems.MAID_MACE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.sonicWave)
            output.accept(InitItems.SONIC_WAVE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.tntCake)
            output.accept(InitItems.TNT_CAKE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.spawnEggs){
            output.accept(InitItems.RAGDOLLABLE_MAID_SPAWN_EGG.get());
            output.accept(InitItems.WINE_FOX_SPAWN_EGG.get());
        }
        output.accept(InitItems.COPY_RAGDOLL_ID_ITEM.get());
    }

    public static void getAllDollItem(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output){
        if(FMLEnvironment.dist.isDedicatedServer())return;
        for (String modelID : CustomPackLoader.MAID_MODELS.getModelIdSet()) {
            var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, modelID.replace(":", "/"));
            if(DefFileLoader.getDefFile(ragdollID) == null)continue;
            var stack = new ItemStack(InitItems.PLAYER_CHEAT_DEATH_ITEM.get(), 1);
            stack.set(InitDataComponents.MAID_MODEL, modelID);
            stack.set(InitDataComponents.MAID_SOUND, DefaultMaidSoundPack.DEFAULT_SOUND_PACK_ID);
            output.accept(stack);
        }
    }

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> createDollTab(DeferredRegister<CreativeModeTab> REGISTRY){
        return REGISTRY.register("doll", r -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.sable_maid_ragdoll.doll"))
                .icon(MaidCreativeTab::getDollDisplayStack)
                .displayItems(MaidCreativeTab::getAllDollItem)
                .withTabsBefore(InitCreativeModeTab.MAIN_TAB.getId())
                .build());
    }
}
