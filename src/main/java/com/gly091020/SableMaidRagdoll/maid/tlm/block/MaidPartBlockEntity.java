package com.gly091020.SableMaidRagdoll.maid.tlm.block;

import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitBlockEntities;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MaidPartBlockEntity extends AbstractPartBlockEntity {
    public MaidPartBlockEntity(BlockPos pos, BlockState state) {
        super(TLMInitBlockEntities.MAID_PART_BLOCK_ENTITY.get(), pos, state);
    }
}
