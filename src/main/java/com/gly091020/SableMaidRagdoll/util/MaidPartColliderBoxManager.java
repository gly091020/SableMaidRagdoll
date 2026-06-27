package com.gly091020.SableMaidRagdoll.util;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.physics.chunk.VoxelNeighborhoodState;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
import dev.ryanhcode.sable.physics.impl.rapier.collider.RapierVoxelColliderData;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

// 大力出奇迹
public class MaidPartColliderBoxManager {
    private static final Map<String, RapierVoxelColliderData> data = new HashMap<>();

    public static void reset(){
        data.clear();
    }

    public static RapierVoxelColliderData getColliderData(MaidPartBlockEntity.RenderData renderData,
                                                          MaidPartBlockEntity.MaidBlockShape blockShape){
        var k = renderData.modelName() + "/" + renderData.partName();
        if(data.containsKey(k))
            return data.get(k);

        var r = createOne(blockShape);
        data.put(k, r);
        return r;
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
