package com.gly091020.SableMaidRagdoll.maid.tlm.init;

import com.gly091020.SableMaidRagdoll.init.InitBlockEntities;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.MaidFairyPartBlockEntity;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.MaidPartBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TLMInitBlockEntities {
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidFairyPartBlockEntity>> MAID_FAIRY_PART_BLOCK_ENTITY;
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidPartBlockEntity>> MAID_PART_BLOCK_ENTITY;
    public static void init() {
        MAID_FAIRY_PART_BLOCK_ENTITY = InitBlockEntities.BLOCK_ENTITIES.register("maid_fairy_part", () ->
                BlockEntityType.Builder.of(MaidFairyPartBlockEntity::new, TLMInitBlocks.MAID_FAIRY_PART_BLOCK.get()).build(null));
        MAID_PART_BLOCK_ENTITY = InitBlockEntities.BLOCK_ENTITIES.register("maid_part", () ->
                BlockEntityType.Builder.of(MaidPartBlockEntity::new, TLMInitBlocks.MAID_PART_BLOCK.get()).build(null));
    }
}