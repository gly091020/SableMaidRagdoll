package com.gly091020.SableMaidRagdoll.block.parts;

import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MaidPartBlockEntity extends AbstractPartBlockEntity {
    public MaidPartBlockEntity(BlockPos pos, BlockState state) {
        super(InitBlockEntities.MAID_PART_BLOCK_ENTITY.get(), pos, state);
    }
}
