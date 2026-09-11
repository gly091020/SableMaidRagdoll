package com.gly091020.SableMaidRagdoll.maid.tlm.init;

import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.MaidFairyPartBlock;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.MaidPartBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TLMInitBlocks {
    public static DeferredHolder<Block, MaidFairyPartBlock> MAID_FAIRY_PART_BLOCK;
    public static DeferredHolder<Block, MaidPartBlock> MAID_PART_BLOCK;
    public static void init() {
        MAID_FAIRY_PART_BLOCK = InitBlocks.BLOCKS.register("maid_fairy_part",
                () -> new MaidFairyPartBlock(MaidPartBlock.PROPERTIES));
        MAID_PART_BLOCK = InitBlocks.BLOCKS.register("maid_part",
                () -> new MaidPartBlock(MaidPartBlock.PROPERTIES));
    }
}