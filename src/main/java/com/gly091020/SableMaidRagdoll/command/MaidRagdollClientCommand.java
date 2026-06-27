package com.gly091020.SableMaidRagdoll.command;

import com.gly091020.SableMaidRagdoll.editor.EditorOpener;
import com.gly091020.SableMaidRagdoll.util.GlobalDebugRenderEnable;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

public class MaidRagdollClientCommand {
    private static final String COMMAND = "sable_maid_ragdoll_client";
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal(COMMAND);
        root.then(Commands.literal("debugRender").executes(MaidRagdollClientCommand::debugRender));
        if(ModList.get().isLoaded("ldlib2"))
            root.then(Commands.literal("editor").executes(MaidRagdollClientCommand::openEditor));
        dispatcher.register(root);
    }

    public static int debugRender(CommandContext<CommandSourceStack> context){
        GlobalDebugRenderEnable.enable = !GlobalDebugRenderEnable.enable;
        context.getSource().sendSuccess(() -> Component.translatable("command.sablemaidragdoll.debug_render.success"), false);
        return 1;
    }

    public static int openEditor(CommandContext<CommandSourceStack> context){
        EditorOpener.openEditor();
        return 1;
    }
}
