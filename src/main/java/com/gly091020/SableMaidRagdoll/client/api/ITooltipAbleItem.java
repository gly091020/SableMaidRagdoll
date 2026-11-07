package com.gly091020.SableMaidRagdoll.client.api;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

public interface ITooltipAbleItem {
    void appendHelpText(List<Component> tooltips);

    void appendBackgroundText(List<Component> tooltips);

    default void generateComponentList(List<Component> tooltips, Item item, int count, boolean isHelp){
        var id = BuiltInRegistries.ITEM.getKey(item);
        var help = isHelp ? "help" : "bg";
        var color = isHelp ? ChatFormatting.WHITE : ChatFormatting.GRAY;
        if(count <= 1){
            tooltips.add(Component.translatable(String.format("item.sablemaidragdoll.%s.tip.%s", id.getPath(), help)).withStyle(color));
            return;
        }
        for (int i = 0; i < count; i++) {
            tooltips.add(Component.translatable(String.format("item.sablemaidragdoll.%s.tip.%s.%d", id.getPath(), help, i + 1)).withStyle(color));
        }
    }
}
