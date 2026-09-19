package com.gly091020.SableMaidRagdoll.maid.lmrb.init;

import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.gly091020.SableMaidRagdoll.maid.lmrb.block.LittleMaidPartBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class LMRBInitBlockEntities {
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<LittleMaidPartBlockEntity>> LITTLE_MAID_PART_BLOCK_ENTITY;

    public static void init() {
        LITTLE_MAID_PART_BLOCK_ENTITY = InitBlockEntities.BLOCK_ENTITIES.register("little_maid_part", () ->
                BlockEntityType.Builder.of(LittleMaidPartBlockEntity::new, LMRBInitBlocks.LITTLE_MAID_PART_BLOCK.get()).build(null));
    }
}
