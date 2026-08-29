package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollBlockEntity;
import com.gly091020.SableMaidRagdoll.block.parts.MaidFairyPartBlockEntity;
import com.gly091020.SableMaidRagdoll.block.parts.MaidPartBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SableMaidRagdoll.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidDollBlockEntity>> MAID_DOLL_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_doll", () ->
            BlockEntityType.Builder.of(MaidDollBlockEntity::new, InitBlocks.MAID_DOLL_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidFairyPartBlockEntity>> MAID_FAIRY_PART_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_fairy_part", () ->
            BlockEntityType.Builder.of(MaidFairyPartBlockEntity::new, InitBlocks.MAID_FAIRY_PART_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidPartBlockEntity>> MAID_PART_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_part", () ->
            BlockEntityType.Builder.of(MaidPartBlockEntity::new, InitBlocks.MAID_PART_BLOCK.get()).build(null));

    public static void init(IEventBus bus){
        BLOCK_ENTITIES.register(bus);
    }
}
