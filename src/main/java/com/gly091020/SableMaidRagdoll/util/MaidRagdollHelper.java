package com.gly091020.SableMaidRagdoll.util;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3dc;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

public class MaidRagdollHelper {
    public static void create(ServerLevel serverLevel, Vec3 pos, String modelName){
        var defFile = MaidPartDefFileLoader.getDefFile(modelName);
        ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
        if(container == null)return;
        container.getAllSubLevels().forEach(subLevel -> subLevel.getUniqueId());

        var allPart = new HashMap<String, ServerSubLevel>();
        for (String part: defFile.parts().keySet()){
            var data = defFile.getPartPosData(part);
            if(data == null)continue;
            var s = createBlock(container, blockEntity ->
            {
                blockEntity.setMaidBlockShape(defFile.createShape(part));
                blockEntity.setRenderData(defFile.createRenderData(modelName, part));
            }, data.getPose(pos));
            if(s != null)
                allPart.put(part, s);
        }

        for (MaidPartDefFileLoader.JointData jointData: defFile.jointData()){
            var a = allPart.get(jointData.partA());
            var b = allPart.get(jointData.partB());
            if(a == null || b == null)continue;

            try{
                createJoint(container, a, b, jointData.getVector3dcA(a), jointData.getVector3dcB(b));
            } catch (Exception e) {
                SableMaidRagdoll.LOGGER.error("连接时错误：", e);
            }
        }
    }

    private static ServerSubLevel createBlock(ServerSubLevelContainer container,
                                              Consumer<MaidPartBlockEntity> dataConsumer,
                                              Pose3d pose3d) {
        final BlockState blockState = SableMaidRagdoll.MAID_PART_BLOCK.get().defaultBlockState();
        if (container == null) return null;
        var subLevel = container.allocateNewSubLevel(pose3d);
        var plot = subLevel.getPlot();
        var center = plot.getCenterChunk();
        plot.newEmptyChunk(center);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO, blockState, 3);
        var be = plot.getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO);
        if (be instanceof MaidPartBlockEntity blockEntity)
            dataConsumer.accept(blockEntity);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.above(), Blocks.GLASS.defaultBlockState(), 3);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO.above(), Blocks.AIR.defaultBlockState(), 3);
        subLevel.updateLastPose();
        return (ServerSubLevel) subLevel;
    }

    // fixme:重进存档后连接消失
    private static void createJoint(ServerSubLevelContainer container, ServerSubLevel subLevel1, ServerSubLevel subLevel2, Vector3dc pos1, Vector3dc pos2){
        container.physicsSystem().getPipeline().addConstraint(subLevel1, subLevel2,
                new GenericConstraintConfiguration(pos1, pos2, new Quaterniond(), new Quaterniond(),
                        Set.of(ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z)));
    }
}
