package com.gly091020.SableMaidRagdoll.command;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class MaidRagdollCommand {
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("maid_ragdoll");
        root.then(Commands.literal("test").executes(MaidRagdollCommand::test));
        dispatcher.register(root);
    }

    public static int test(CommandContext<CommandSourceStack> context) {
        createBlock(context, blockEntity ->
        {
            blockEntity.setMaidBlockShape(new MaidPartBlockEntity.MaidBlockShape(
                    List.of(new MaidPartBlockEntity.Box(0.25f, 0.25f - (float) 1 / 16 * 2, 0.25f, 0.75f, 0.75f - (float) 1 / 16 * 2, 0.75f))
            ));
            blockEntity.setRenderData(new MaidPartBlockEntity.RenderData(
                    "authors_and_credits:wine_fox_taisho",
                    "head",
                    Vec3.ZERO,
                    Vec3.ZERO
            ));
        });
        return 1;
    }

    private static void createBlock(CommandContext<CommandSourceStack> context, Consumer<MaidPartBlockEntity> consumer) {
        SubLevelContainer container = SubLevelContainer.getContainer(context.getSource().getLevel());
        if (container == null) return;
        var pos = new Pose3d();
        var playerPos = context.getSource().getPosition();
        pos.position().set(playerPos.x, playerPos.y, playerPos.z);
        var subLevel = container.allocateNewSubLevel(pos);
        var plot = subLevel.getPlot();
        var center = plot.getCenterChunk();
        plot.newEmptyChunk(center);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO, SableMaidRagdoll.MAID_PART_BLOCK.get().defaultBlockState(), 3);
        var be = plot.getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO);
        if (be instanceof MaidPartBlockEntity blockEntity)
            consumer.accept(blockEntity);
        subLevel.updateLastPose();
    }
}
