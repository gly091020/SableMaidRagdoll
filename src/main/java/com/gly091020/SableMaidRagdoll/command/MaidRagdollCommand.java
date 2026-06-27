package com.gly091020.SableMaidRagdoll.command;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public class MaidRagdollCommand {
    public static final String COMMAND = "sable_maid_ragdoll";
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal(COMMAND);
        root.requires(source -> source.hasPermission(2));
        root.then(Commands.literal("spawn")
                .then(Commands.argument("model", ResourceLocationArgument.id())
                        .suggests(MaidRagdollCommand::suggestionModelNames)
                        .executes(MaidRagdollCommand::spawn)));
        root.then(Commands.literal("reload").executes(MaidRagdollCommand::reload));
        root.then(Commands.literal("spawn_test_maid").executes(MaidRagdollCommand::spawnTestMaid));
        dispatcher.register(root);
    }

    public static int spawnTestMaid(CommandContext<CommandSourceStack> context){
        var maid = new EntityMaid(context.getSource().getLevel());
        maid.setPos(context.getSource().getPosition());
        maid.setHealth(1);
        maid.setModelId("authors_and_credits:wine_fox_taisho");

        context.getSource().getLevel().addFreshEntity(maid);
        return 1;
    }

    public static int spawn(CommandContext<CommandSourceStack> context) {
        String modelName = ResourceLocationArgument.getId(context, "model").toString();
        if(!MaidRagdollHelper.create(context.getSource().getLevel(),
                context.getSource().getPosition(), modelName).isEmpty()){
            context.getSource().sendSuccess(() -> Component.translatable("command.sablemaidragdoll.spawn.success"), false);
            return 1;
        }
        context.getSource().sendFailure(Component.translatable("command.sablemaidragdoll.spawn.failed"));
        return 0;
    }

    public static int reload(CommandContext<CommandSourceStack> context){
        MaidPartDefFileLoader.init();
        context.getSource().sendSuccess(() -> Component.translatable("command.sablemaidragdoll.reload.success"), false);
        return 1;
    }

    public static CompletableFuture<Suggestions> suggestionModelNames(CommandContext<?> ctx, SuggestionsBuilder builder){
        MaidPartDefFileLoader.getDefFileMap().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }
}
