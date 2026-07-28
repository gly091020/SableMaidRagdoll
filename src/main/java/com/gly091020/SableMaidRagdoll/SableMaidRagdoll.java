package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableMaidRagdoll.block.MaidPartBlock;
import com.gly091020.SableMaidRagdoll.block.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.item.CheatDeathBaubleItem;
import com.gly091020.SableMaidRagdoll.item.CopyRagdollIDItem;
import com.gly091020.SableMaidRagdoll.item.PlayerCheatDeathItem;
import com.gly091020.SableRagdollLib.api.RagdollTypeRegistry;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
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
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final TagKey<Item> MAID_TO_RAGDOLL_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "maid_to_ragdoll"));
    public static final TagKey<DamageType> ALWAYS_TO_RAGDOLL_TAG = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "always_to_ragdoll"));

    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE = SOUNDS.register("pipe", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "pipe")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HUNGRY = SOUNDS.register("hungry", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "hungry")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DROP = SOUNDS.register("drop", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "drop")));

    public static final DeferredHolder<Item, CheatDeathBaubleItem> CHEAT_DEATH_BAUBLE_ITEM = ITEMS.register("cheat_death_bauble", resourceLocation -> new CheatDeathBaubleItem());
    public static final DeferredHolder<Item, PlayerCheatDeathItem> PLAYER_CHEAT_DEATH_ITEM = ITEMS.register("player_cheat_death", resourceLocation -> new PlayerCheatDeathItem());
    public static final DeferredHolder<Item, CopyRagdollIDItem> COPY_RAGDOLL_ID_ITEM = ITEMS.register("copy_ragdoll_id", resourceLocation -> new CopyRagdollIDItem());
    public static final DeferredHolder<Block, MaidPartBlock> MAID_PART_BLOCK = BLOCKS.register("maid_part", () -> new MaidPartBlock(MaidPartBlock.PROPERTIES));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidPartBlockEntity>> MAID_PART_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_part", () ->
            BlockEntityType.Builder.of(MaidPartBlockEntity::new, MAID_PART_BLOCK.get()).build(null));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MAID_MODEL = DATA_COMPONENTS.register("maid_model", r ->
       DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
    );

    public static final ResourceLocation RAGDOLL_TYPE = ResourceLocation.fromNamespaceAndPath(MODID, "maid");
    public static SableMaidRagdollConfig CONFIG;

    public SableMaidRagdoll(IEventBus bus){
        CONFIG = AutoConfig.register(SableMaidRagdollConfig.class, Toml4jConfigSerializer::new).getConfig();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        SOUNDS.register(bus);
        DATA_COMPONENTS.register(bus);

        RagdollTypeRegistry.registry(RAGDOLL_TYPE, MAID_PART_BLOCK::get, MAID_PART_BLOCK_ENTITY::get);
    }
}
