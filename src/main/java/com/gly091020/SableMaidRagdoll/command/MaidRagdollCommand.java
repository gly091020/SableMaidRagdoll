package com.gly091020.SableMaidRagdoll.command;

import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MaidRagdollCommand {
    public static final String COMMAND = "sable_maid_ragdoll";
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal(COMMAND);
        root.requires(source -> source.hasPermission(2));

        MaidRagdollTypesManager.appendCommand(root);

        dispatcher.register(root);
    }
}
