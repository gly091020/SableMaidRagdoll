package com.gly091020.SableMaidRagdoll.command;

import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MaidRagdollCommand {
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("maid_ragdoll");
        root.then(Commands.literal("test_ragdoll").executes(MaidRagdollCommand::test));
        root.then(Commands.literal("reload").executes(MaidRagdollCommand::reload));
        dispatcher.register(root);
    }

    public static int test(CommandContext<CommandSourceStack> context) {
        final String modelName = "authors_and_credits:wine_fox_taisho";
        MaidRagdollHelper.create(context.getSource().getLevel(),
                context.getSource().getPosition(), modelName);
        return 1;
    }

    public static int reload(CommandContext<CommandSourceStack> context){
        MaidPartDefFileLoader.init();
        return 1;
    }
}
