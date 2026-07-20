package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableMaidRagdoll.block.MaidPartBlock;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.editor.MaidRagdollEditorRegistry;
import com.gly091020.SableMaidRagdoll.item.CheatDeathBaubleItem;
import com.gly091020.SableMaidRagdoll.item.CopyRagdollIDItem;
import com.gly091020.SableRagdollLib.SableRagdollLib;
import com.gly091020.SableRagdollLib.api.RagdollTypeRegistry;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(SableMaidRagdoll.MODID)
public class SableMaidRagdoll {
    public static final String MODID = "sablemaidragdoll";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredHolder<Item, CheatDeathBaubleItem> CHEAT_DEATH_BAUBLE_ITEM = ITEMS.register("cheat_death_bauble", resourceLocation -> new CheatDeathBaubleItem());
    public static final DeferredHolder<Item, CopyRagdollIDItem> COPY_RAGDOLL_ID_ITEM = ITEMS.register("copy_ragdoll_id", resourceLocation -> new CopyRagdollIDItem());
    public static final DeferredHolder<Block, MaidPartBlock> MAID_PART_BLOCK = BLOCKS.register("maid_part", () -> new MaidPartBlock(MaidPartBlock.PROPERTIES));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidPartBlockEntity>> MAID_PART_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_part", () ->
            BlockEntityType.Builder.of(MaidPartBlockEntity::new, MAID_PART_BLOCK.get()).build(null));

    public static final ResourceLocation RAGDOLL_TYPE = ResourceLocation.fromNamespaceAndPath(MODID, "maid");
    public static SableMaidRagdollConfig CONFIG;

    public SableMaidRagdoll(IEventBus bus){
        CONFIG = AutoConfig.register(SableMaidRagdollConfig.class, Toml4jConfigSerializer::new).getConfig();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);

        RagdollTypeRegistry.registry(RAGDOLL_TYPE, MAID_PART_BLOCK::get, MAID_PART_BLOCK_ENTITY::get);
    }
}
