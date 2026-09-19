package com.gly091020.SableMaidRagdoll.maid.lmrb.init;

import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.maid.lmrb.block.LittleMaidPartBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

public class LMRBInitBlocks {
    public static DeferredHolder<Block, LittleMaidPartBlock> LITTLE_MAID_PART_BLOCK;

    public static void init() {
        LITTLE_MAID_PART_BLOCK = InitBlocks.BLOCKS.register("little_maid_part",
                () -> new LittleMaidPartBlock(LittleMaidPartBlock.PROPERTIES));
    }
}
