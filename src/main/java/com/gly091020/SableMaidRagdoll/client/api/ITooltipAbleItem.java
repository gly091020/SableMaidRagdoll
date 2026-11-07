package com.gly091020.SableMaidRagdoll.client.api;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface ITooltipAbleItem {
    void appendHelpText(List<Component> tooltips);

    void appendBackgroundText(List<Component> tooltips);
}
