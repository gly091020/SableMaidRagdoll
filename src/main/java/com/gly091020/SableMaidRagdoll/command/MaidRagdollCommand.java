package com.gly091020.SableMaidRagdoll.command;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MaidRagdollCommand {
    public static final String COMMAND = "sable_maid_ragdoll";
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal(COMMAND);
        root.requires(source -> source.hasPermission(2));
        root.then(Commands.literal("spawn_test_maid").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(MaidRagdollCommand::spawnTestMaid)));
        dispatcher.register(root);
    }

    public static int spawnTestMaid(CommandContext<CommandSourceStack> context) {
        var modelID = ResourceLocationArgument.getId(context, "modelID").toString();
        var maid = new EntityMaid(context.getSource().getLevel());
        maid.setPos(context.getSource().getPosition());
        maid.setHealth(1);
        maid.setModelId(modelID);

        var maxHealth = maid.getAttribute(Attributes.MAX_HEALTH);
        if(maxHealth != null)maxHealth.setBaseValue(1);
        var moveSpeed = maid.getAttribute(Attributes.MOVEMENT_SPEED);
        if(moveSpeed != null)moveSpeed.setBaseValue(0);

        context.getSource().getLevel().addFreshEntity(maid);
        return 1;
    }
}
