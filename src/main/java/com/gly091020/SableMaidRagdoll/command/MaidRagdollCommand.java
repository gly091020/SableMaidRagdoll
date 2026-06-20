package com.gly091020.SableMaidRagdoll.command;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;

import java.util.concurrent.CompletableFuture;

public class MaidRagdollCommand {
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal(SableMaidRagdoll.MODID);
        root.requires(source -> source.hasPermission(2));
        root.then(Commands.literal("spawn")
                .then(Commands.argument("model", ResourceLocationArgument.id())
                        .suggests(MaidRagdollCommand::suggestionModelNames)
                        .executes(MaidRagdollCommand::spawn)));
        root.then(Commands.literal("reload").executes(MaidRagdollCommand::reload));
        dispatcher.register(root);
    }

    public static int spawn(CommandContext<CommandSourceStack> context) {
        String modelName = ResourceLocationArgument.getId(context, "model").toString();
        MaidRagdollHelper.create(context.getSource().getLevel(),
                context.getSource().getPosition(), modelName);
        return 1;
    }

    public static int reload(CommandContext<CommandSourceStack> context){
        MaidPartDefFileLoader.init();
        return 1;
    }

    public static CompletableFuture<Suggestions> suggestionModelNames(CommandContext<?> ctx, SuggestionsBuilder builder){
        MaidPartDefFileLoader.getDefFileMap().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }
}
