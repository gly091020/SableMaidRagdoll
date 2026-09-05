package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollBlock;
import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlock;
import com.gly091020.SableMaidRagdoll.block.parts.MaidFairyPartBlock;
import com.gly091020.SableMaidRagdoll.block.parts.MaidPartBlock;
import com.gly091020.SableMaidRagdoll.block.tnt_cake.TNTCakeBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, SableMaidRagdoll.MODID);
    public static final DeferredHolder<Block, TNTCakeBlock> TNT_CAKE_BLOCK = BLOCKS.register("tnt_cake", r -> new TNTCakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));
    public static final DeferredHolder<Block, MaidDollBlock> MAID_DOLL_BLOCK = BLOCKS.register("maid_doll", () -> new MaidDollBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).isValidSpawn(Blocks::never)));
    public static final DeferredHolder<Block, MaidFairyPartBlock> MAID_FAIRY_PART_BLOCK = BLOCKS.register("maid_fairy_part", () -> new MaidFairyPartBlock(MaidPartBlock.PROPERTIES));
    public static final DeferredHolder<Block, MaidPartBlock> MAID_PART_BLOCK = BLOCKS.register("maid_part", () -> new MaidPartBlock(MaidPartBlock.PROPERTIES));
    public static final DeferredHolder<Block, MobCannonBlock> MOB_CANNON_BLOCK = BLOCKS.register("mob_cannon", () -> new MobCannonBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DISPENSER)));

    public static void init(IEventBus bus){
        BLOCKS.register(bus);
    }
}
