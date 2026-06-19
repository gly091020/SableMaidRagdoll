package com.gly091020.SableMaidRagdoll.util;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.physics.chunk.VoxelNeighborhoodState;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import dev.ryanhcode.sable.physics.impl.rapier.collider.RapierVoxelColliderData;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MaidPartColliderBoxManager {
    private static final Map<MaidPartBlockEntity.MaidBlockShape, RapierVoxelColliderData> data = new HashMap<>();

    public static void init(){
        final int count = 16;
        for (int x = 0; x < count; x++) {
            for (int y = 0; y < count; y++) {
                for (int z = 0; z < count; z++) {
                    var shape = new MaidPartBlockEntity.MaidBlockShape(
                            Collections.singletonList(MaidPartBlockEntity.Box.fromSize(x, y, z))
                    );
                    data.put(shape, createOne(shape));
                }
            }
        }
    }

    public static void reset(){
        data.clear();
    }

    public static RapierVoxelColliderData getColliderData(MaidPartBlockEntity.MaidBlockShape shape){
        if(data.containsKey(shape))
            return data.get(shape);

        return RapierVoxelColliderData.EMPTY;
    }

    public static RapierVoxelColliderData createOne(MaidPartBlockEntity.MaidBlockShape shape){
        var childState = SableMaidRagdoll.MAID_PART_BLOCK.get().defaultBlockState();
        final boolean liquid = VoxelNeighborhoodState.isLiquid(childState);

        final double friction = PhysicsBlockPropertyHelper.getFriction(childState);
        final double volume = PhysicsBlockPropertyHelper.getVolume(childState);
        final double restitution = PhysicsBlockPropertyHelper.getRestitution(childState);
        final BlockSubLevelCollisionCallback callback = BlockWithSubLevelCollisionCallback.sable$getCallback(childState);
        final RapierVoxelColliderData entry = Rapier3D.createVoxelColliderEntry(friction, volume, restitution, liquid, callback);

        for(MaidPartBlockEntity.Box box: shape.boxes())
            entry.addBox(new Vector3d(box.minX(), box.minY(), box.minZ()), new Vector3d(box.maxX(), box.maxY(), box.maxZ()));

        return entry;
    }
}
