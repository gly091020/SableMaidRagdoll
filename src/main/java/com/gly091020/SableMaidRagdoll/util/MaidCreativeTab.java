package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.DefaultMaidSoundPack;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
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
        var stack = new ItemStack(SableMaidRagdoll.PLAYER_CHEAT_DEATH_ITEM.get(), 1);
        stack.set(SableMaidRagdoll.MAID_MODEL, "authors_and_credits:wine_fox_taisho");
        return stack;
    }

    public static void getAllMainItem(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output){
        if(FMLEnvironment.dist.isDedicatedServer())return;
        if(SableMaidRagdoll.CONFIG.items.playerCheatDeathItem)
            output.accept(SableMaidRagdoll.PLAYER_CHEAT_DEATH_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.cheatDeathBauble)
            output.accept(SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.maidMace)
            output.accept(SableMaidRagdoll.MAID_MACE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.sonicWave)
            output.accept(SableMaidRagdoll.SONIC_WAVE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.tntCake)
            output.accept(SableMaidRagdoll.TNT_CAKE_ITEM.get());
        if(SableMaidRagdoll.CONFIG.items.spawnEggs){
            output.accept(SableMaidRagdoll.RAGDOLLABLE_MAID_SPAWN_EGG.get());
            output.accept(SableMaidRagdoll.WINE_FOX_SPAWN_EGG.get());
        }
        output.accept(SableMaidRagdoll.COPY_RAGDOLL_ID_ITEM.get());
    }

    public static void getAllDollItem(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output){
        if(FMLEnvironment.dist.isDedicatedServer())return;
        for (String modelID : CustomPackLoader.MAID_MODELS.getModelIdSet()) {
            var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, modelID.replace(":", "/"));
            if(DefFileLoader.getDefFile(ragdollID) == null)continue;
            var stack = new ItemStack(SableMaidRagdoll.PLAYER_CHEAT_DEATH_ITEM.get(), 1);
            stack.set(SableMaidRagdoll.MAID_MODEL, modelID);
            stack.set(SableMaidRagdoll.MAID_SOUND, DefaultMaidSoundPack.DEFAULT_SOUND_PACK_ID);
            output.accept(stack);
        }
    }

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> createDollTab(DeferredRegister<CreativeModeTab> REGISTRY){
        return REGISTRY.register("doll", r -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.sable_maid_ragdoll.doll"))
                .icon(MaidCreativeTab::getDollDisplayStack)
                .displayItems(MaidCreativeTab::getAllDollItem)
                .withTabsBefore(SableMaidRagdoll.MAIN_TAB.getId())
                .build());
    }
}
