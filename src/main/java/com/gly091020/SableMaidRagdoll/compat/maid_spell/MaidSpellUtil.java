package com.gly091020.SableMaidRagdoll.compat.maid_spell;

import com.github.yimeng261.maidspell.compat.irons_spellbooks.entity.winefox.MagicalWinefoxBossEntity;
import com.github.yimeng261.maidspell.compat.irons_spellbooks.entity.winefox.WinefoxDialogue;
import com.github.yimeng261.maidspell.item.MaidSpellItems;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class MaidSpellUtil {
    public static boolean isStellarWitch(Entity entity){
        if(!CompatMods.IRON_SPELLBOOKS.isLoaded())return false;
        return entity instanceof MagicalWinefoxBossEntity;
    }

    public static void stellarWitchSpeak(Player player, Component component){
        if(!CompatMods.IRON_SPELLBOOKS.isLoaded())return;
        WinefoxDialogue.sendToPlayer(player, component);
    }

    public static Component getRandomCrystalSpeak(){
        return Component.translatable("dialogue.touhou_little_maid_spell.winefox.crystal_no_ragdoll_" + (ThreadLocalRandom.current().nextInt(3) + 1));
    }

    public static Component getRandomItemSpeak(){
        return Component.translatable("dialogue.touhou_little_maid_spell.winefox.item_no_ragdoll_" + (ThreadLocalRandom.current().nextInt(3) + 1));
    }

    public static Component getVictorySpeak(){
        return Component.translatable("dialogue.touhou_little_maid_spell.winefox.victory_ragdoll_" + (ThreadLocalRandom.current().nextInt(10) + 1));
    }

    public static boolean isStarlightDagger(ItemStack stack){
        return stack.is(MaidSpellItems.STARGLINT_DAGGER);
    }
}
