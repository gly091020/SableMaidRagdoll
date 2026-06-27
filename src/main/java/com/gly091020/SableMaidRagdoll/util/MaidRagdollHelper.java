package com.gly091020.SableMaidRagdoll.util;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
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
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

public class MaidRagdollHelper {
    public static boolean create(ServerLevel serverLevel, Vec3 pos, String modelName){
        var defFile = MaidPartDefFileLoader.getDefFile(modelName);
        ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
        if(container == null)return false;
        if(defFile == null)return false;

        var allPart = new HashMap<String, ServerSubLevel>();
        var allBE = new HashMap<String, MaidPartBlockEntity>();
        for (String part: defFile.parts().keySet()){
            var data = defFile.getPartPosData(part);
            if(data == null)continue;
            var s = createBlock(container, blockEntity ->
            {
                blockEntity.setMaidBlockShape(defFile.createShape(part));
                blockEntity.setRenderData(defFile.createRenderData(modelName, part));
                blockEntity.setHidePart(defFile.hideParts());
                allBE.put(part, blockEntity);
            }, data.getPose(pos));
            if(s != null)
                allPart.put(part, s);
        }
        if(allBE.isEmpty())return false;
        MaidPartBlockEntity body = allBE.values().stream().toList().getFirst();

        var data = new ArrayList<MaidPartBlockEntity.BEJointData>();
        for (MaidPartDefFileLoader.JointData jointData: defFile.jointData()){
            var a = allPart.get(jointData.partA());
            var b = allPart.get(jointData.partB());
            if(a == null || b == null)continue;

            var pos1 = jointData.getVector3dcA(a);
            var pos2 = jointData.getVector3dcB(b);
            data.add(new MaidPartBlockEntity.BEJointData(a.getUniqueId(),
                    b.getUniqueId(),
                    new Vec3(pos1.x(), pos1.y(), pos1.z()),
                    new Vec3(pos2.x(), pos2.y(), pos2.z()),
                    jointData.motor(),
                    jointData.contacts()
            ));
        }
        body.setJointData(data);
        return true;
    }

    public static ServerSubLevel createBlock(ServerSubLevelContainer container,
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

    public static PhysicsConstraintHandle createJoint(ServerSubLevelContainer container,
                                                      ServerSubLevel subLevel1,
                                                      ServerSubLevel subLevel2,
                                                      Vector3dc pos1,
                                                      Vector3dc pos2){
        return container.physicsSystem().getPipeline().addConstraint(subLevel1, subLevel2,
                new GenericConstraintConfiguration(pos1, pos2, new Quaterniond(), new Quaterniond(),
                        Set.of(ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z)));
    }

    public static PhysicsConstraintHandle createJoint(ServerSubLevelContainer container,
                                   ServerSubLevel subLevel1,
                                   ServerSubLevel subLevel2,
                                   Vec3 pos1,
                                   Vec3 pos2){
        return createJoint(container, subLevel1, subLevel2, new Vector3d(
                pos1.x, pos1.y, pos1.z
        ), new Vector3d(
                pos2.x, pos2.y, pos2.z
        ));
    }
}
