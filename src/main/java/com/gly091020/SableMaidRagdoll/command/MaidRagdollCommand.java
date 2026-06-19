package com.gly091020.SableMaidRagdoll.command;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class MaidRagdollCommand {
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("maid_ragdoll");
        root.then(Commands.literal("test_ragdoll").executes(MaidRagdollCommand::test));
        dispatcher.register(root);
    }

    public static int test(CommandContext<CommandSourceStack> context) {
//        createBlock(context, blockEntity ->
//        {
//            blockEntity.setMaidBlockShape(new MaidPartBlockEntity.MaidBlockShape(
//                    List.of(MaidPartBlockEntity.Box.fromSize(8, 8, 8))
//            ));
//            blockEntity.setRenderData(new MaidPartBlockEntity.RenderData(
//                    "authors_and_credits:wine_fox_taisho",
//                    "head",
//                    new Vec3(0, 18 / 16f, 0),
//                    Vec3.ZERO
//            ));
//        });
//        createBlock(context, blockEntity ->
//        {
//            blockEntity.setMaidBlockShape(new MaidPartBlockEntity.MaidBlockShape(
//                    List.of(MaidPartBlockEntity.Box.fromSize(6, 9, 5))
//            ));
//            blockEntity.setRenderData(new MaidPartBlockEntity.RenderData(
//                    "authors_and_credits:wine_fox_taisho",
//                    "body|sittingRotationSkirt",
//                    new Vec3(0, 8.75 / 16, 0),
//                    Vec3.ZERO
//            ));
//        });
//        createBlock(context, blockEntity ->
//        {
//            blockEntity.setMaidBlockShape(new MaidPartBlockEntity.MaidBlockShape(
//                    List.of(MaidPartBlockEntity.Box.fromSize(2, 8, 2))
//            ));
//            blockEntity.setRenderData(new MaidPartBlockEntity.RenderData(
//                    "authors_and_credits:wine_fox_taisho",
//                    "armLeft",
//                    new Vec3(-3 / 16f, 9.5 / 16, 0),
//                    new Vec3(0, 0, Math.toRadians(10))
//            ));
//        });
//        createBlock(context, blockEntity ->
//        {
//            blockEntity.setMaidBlockShape(new MaidPartBlockEntity.MaidBlockShape(
//                    List.of(MaidPartBlockEntity.Box.fromSize(2, 8, 2))
//            ));
//            blockEntity.setRenderData(new MaidPartBlockEntity.RenderData(
//                    "authors_and_credits:wine_fox_taisho",
//                    "armRight",
//                    new Vec3(3 / 16f, 9.5 / 16, 0),
//                    new Vec3(0, 0, Math.toRadians(-10))
//            ));
//        });
//        createBlock(context, blockEntity ->
//        {
//            blockEntity.setMaidBlockShape(new MaidPartBlockEntity.MaidBlockShape(
//                    List.of(MaidPartBlockEntity.Box.fromSize(3 * 1.2f, 9, 3 * 1.2f))
//            ));
//            blockEntity.setRenderData(new MaidPartBlockEntity.RenderData(
//                    "authors_and_credits:wine_fox_taisho",
//                    "legLeft",
//                    new Vec3(-1.75 / 16f, 0, 0),
//                    Vec3.ZERO
//            ));
//        });
//        createBlock(context, blockEntity ->
//        {
//            blockEntity.setMaidBlockShape(new MaidPartBlockEntity.MaidBlockShape(
//                    List.of(MaidPartBlockEntity.Box.fromSize(3 * 1.2f, 9, 3 * 1.2f))
//            ));
//            blockEntity.setRenderData(new MaidPartBlockEntity.RenderData(
//                    "authors_and_credits:wine_fox_taisho",
//                    "legRight",
//                    new Vec3(1.75 / 16f, 0, 0),
//                    Vec3.ZERO
//            ));
//        });
        final String modelName = "authors_and_credits:wine_fox_taisho";
        var defFile = MaidPartDefFileLoader.getDefFile(modelName);
        for (String part: defFile.parts().keySet()){
            createBlock(context, blockEntity ->
            {
                blockEntity.setMaidBlockShape(defFile.createShape(part));
                blockEntity.setRenderData(defFile.createRenderData(modelName, part));
            });
        }
        return 1;
    }

    private static void createBlock(CommandContext<CommandSourceStack> context, Consumer<MaidPartBlockEntity> consumer) {
        final BlockState blockState = SableMaidRagdoll.MAID_PART_BLOCK.get().defaultBlockState();
        ServerSubLevelContainer container = SubLevelContainer.getContainer(context.getSource().getLevel());
        if (container == null) return;
        var pos = new Pose3d();
        var playerPos = context.getSource().getPosition();
        pos.position().set(playerPos.x, playerPos.y, playerPos.z);
        var subLevel = container.allocateNewSubLevel(pos);
        var plot = subLevel.getPlot();
        var center = plot.getCenterChunk();
        plot.newEmptyChunk(center);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO, blockState, 3);
        var be = plot.getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO);
        if (be instanceof MaidPartBlockEntity blockEntity)
            consumer.accept(blockEntity);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.above(), Blocks.GLASS.defaultBlockState(), 3);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.above(), Blocks.AIR.defaultBlockState(), 3);
        subLevel.updateLastPose();
    }
}
