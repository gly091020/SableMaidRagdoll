package com.gly091020.SableMaidRagdoll;

import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollBlock;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollBlockEntity;
import com.gly091020.SableMaidRagdoll.block.parts.MaidFairyPartBlock;
import com.gly091020.SableMaidRagdoll.block.parts.MaidFairyPartBlockEntity;
import com.gly091020.SableMaidRagdoll.block.parts.MaidPartBlock;
import com.gly091020.SableMaidRagdoll.block.parts.MaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.block.tnt_cake.TNTCakeBlock;
import com.gly091020.SableMaidRagdoll.compat.player_ragdoll.PlayerRagdollUtil;
import com.gly091020.SableMaidRagdoll.item.spawn_egg.RagdollableMaidSpawnEgg;
import com.gly091020.SableMaidRagdoll.item.spawn_egg.WineFoxSpawnEgg;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollPartRecognizer;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.RagdollSaddleLaunch;
import com.gly091020.SableMaidRagdoll.compat.util.CompatMods;
import com.gly091020.SableMaidRagdoll.datagen.SableMaidRagdollDatagen;
import com.gly091020.SableMaidRagdoll.item.CheatDeathBaubleItem;
import com.gly091020.SableMaidRagdoll.item.CopyRagdollIDItem;
import com.gly091020.SableMaidRagdoll.item.MaidMaceItem;
import com.gly091020.SableMaidRagdoll.item.PlayerCheatDeathItem;
import com.gly091020.SableMaidRagdoll.item.SonicWaveItem;
import com.gly091020.SableMaidRagdoll.network.ServerboundBroomManPacket;
import com.gly091020.SableMaidRagdoll.network.ServerboundEmojiSelectPacket;
import com.gly091020.SableMaidRagdoll.util.MaidCreativeTab;
import com.gly091020.SableRagdollLib.api.RagdollTypeRegistry;
import com.gly091020.SableRagdollLib.api.control.RagdollPartRecognizerRegistry;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.UUID;

@Mod(SableMaidRagdoll.MODID)
public class SableMaidRagdoll {
    public static final String MODID = "sablemaidragdoll";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final UUID GLY = UUID.fromString("91bd580f-5f17-4e30-872f-2e480dd9a220");

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    public static final DeferredRegister<ResourceLocation> CUSTOM_STAT = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT, MODID);

    public static final TagKey<Item> MAID_TO_RAGDOLL_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "maid_to_ragdoll"));
    public static final TagKey<DamageType> ALWAYS_TO_RAGDOLL_TAG = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "always_to_ragdoll"));

    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE = SOUNDS.register("pipe", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "pipe")));
    public static final DeferredHolder<SoundEvent, SoundEvent> HUNGRY = SOUNDS.register("hungry", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "hungry")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DROP = SOUNDS.register("drop", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "drop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BIG_DOG = SOUNDS.register("big_dog", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "big_dog")));
    public static final DeferredHolder<SoundEvent, SoundEvent> DOG_CALL = SOUNDS.register("dog_call", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "dog_call")));
    public static final DeferredHolder<SoundEvent, SoundEvent> GCJ_SOUND = SOUNDS.register("gcj_sound", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "gcj_sound")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERMELON_HURT = SOUNDS.register("watermelon_hurt", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "watermelon_hurt")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BROOM_MAN = SOUNDS.register("broom_man", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "broom_man")));

    public static final DeferredHolder<Block, MaidPartBlock> MAID_PART_BLOCK = BLOCKS.register("maid_part", () -> new MaidPartBlock(MaidPartBlock.PROPERTIES));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidPartBlockEntity>> MAID_PART_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_part", () ->
            BlockEntityType.Builder.of(MaidPartBlockEntity::new, MAID_PART_BLOCK.get()).build(null));
    public static final DeferredHolder<Block, MaidFairyPartBlock> MAID_FAIRY_PART_BLOCK = BLOCKS.register("maid_fairy_part", () -> new MaidFairyPartBlock(MaidPartBlock.PROPERTIES));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidFairyPartBlockEntity>> MAID_FAIRY_PART_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_fairy_part", () ->
            BlockEntityType.Builder.of(MaidFairyPartBlockEntity::new, MAID_FAIRY_PART_BLOCK.get()).build(null));
    public static final DeferredHolder<Block, MaidDollBlock> MAID_DOLL_BLOCK = BLOCKS.register("maid_doll", () -> new MaidDollBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).isValidSpawn(Blocks::never)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaidDollBlockEntity>> MAID_DOLL_BLOCK_ENTITY = BLOCK_ENTITIES.register("maid_doll", () ->
            BlockEntityType.Builder.of(MaidDollBlockEntity::new, MAID_DOLL_BLOCK.get()).build(null));
    public static final DeferredHolder<Block, TNTCakeBlock> TNT_CAKE_BLOCK = BLOCKS.register("tnt_cake", r -> new TNTCakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    public static final DeferredHolder<Item, Item> MOD_ICON_ITEM = ITEMS.register("mod_icon", resourceLocation -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, CheatDeathBaubleItem> CHEAT_DEATH_BAUBLE_ITEM = ITEMS.register("cheat_death_bauble", resourceLocation -> new CheatDeathBaubleItem());
    public static final DeferredHolder<Item, PlayerCheatDeathItem> PLAYER_CHEAT_DEATH_ITEM = ITEMS.register("player_cheat_death", resourceLocation -> new PlayerCheatDeathItem());
    public static final DeferredHolder<Item, CopyRagdollIDItem> COPY_RAGDOLL_ID_ITEM = ITEMS.register("copy_ragdoll_id", resourceLocation -> new CopyRagdollIDItem());
    public static final DeferredHolder<Item, MaidMaceItem> MAID_MACE_ITEM = ITEMS.register("maid_mace", resourceLocation -> new MaidMaceItem());
    public static final DeferredHolder<Item, SonicWaveItem> SONIC_WAVE_ITEM = ITEMS.register("sonic_wave", resourceLocation -> new SonicWaveItem());
    public static final DeferredHolder<Item, BlockItem> TNT_CAKE_ITEM = ITEMS.register("tnt_cake", r -> new BlockItem(TNT_CAKE_BLOCK.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, RagdollableMaidSpawnEgg> RAGDOLLABLE_MAID_SPAWN_EGG = ITEMS.register("ragdollanle_maid_spawn_egg", r -> new RagdollableMaidSpawnEgg());
    public static final DeferredHolder<Item, WineFoxSpawnEgg> WINE_FOX_SPAWN_EGG = ITEMS.register("winefox_spawn_egg", r -> new WineFoxSpawnEgg());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MAID_MODEL = DATA_COMPONENTS.register("maid_model", r ->
       DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MAID_SOUND = DATA_COMPONENTS.register("maid_sound", r ->
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ENABLE_CONTROL = DATA_COMPONENTS.register("enable_control", r ->
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            r -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sable_maid_ragdoll.main"))
                    .icon(() -> MOD_ICON_ITEM.get().getDefaultInstance())
                    .withTabsBefore(InitCreativeTabs.MAIN_TAB.getId())
                    .displayItems(MaidCreativeTab::getAllMainItem)
                    .build());
    public static DeferredHolder<CreativeModeTab, CreativeModeTab> DOLL_TAB;

    public static DeferredHolder<ResourceLocation, ResourceLocation> MAID_KNOCKED_AWAY = CUSTOM_STAT.register("maid_knocked_away", r -> r);
    public static DeferredHolder<ResourceLocation, ResourceLocation> TO_MAID = CUSTOM_STAT.register("to_maid", r -> r);

    public static final ResourceLocation RAGDOLL_TYPE = ResourceLocation.fromNamespaceAndPath(MODID, "maid");
    public static final ResourceLocation FAIRY_RAGDOLL_TYPE = ResourceLocation.fromNamespaceAndPath(MODID, "maid_fairy");
    public static final ResourceLocation EMPTY_EMOJI = ResourceLocation.fromNamespaceAndPath(MODID, "empty");
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ResourceLocation>> EMOJI_ATTACHMENT = ATTACHMENT_TYPES.register("emoji", () ->
            AttachmentType.builder(() -> EMPTY_EMOJI).serialize(ResourceLocation.CODEC).sync(ResourceLocation.STREAM_CODEC).build());
    public static SableMaidRagdollConfig CONFIG;

    public SableMaidRagdoll(IEventBus bus){
        CONFIG = AutoConfig.register(SableMaidRagdollConfig.class, Toml4jConfigSerializer::new).getConfig();

        ITEMS.register(bus);
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        SOUNDS.register(bus);
        DATA_COMPONENTS.register(bus);
        ATTACHMENT_TYPES.register(bus);
        CUSTOM_STAT.register(bus);

        if(CONFIG.items.enableDollTab)
            DOLL_TAB = MaidCreativeTab.createDollTab(CREATIVE_TABS);
        CREATIVE_TABS.register(bus);

        RagdollTypeRegistry.registry(RAGDOLL_TYPE, MAID_PART_BLOCK::get, MAID_PART_BLOCK_ENTITY::get);
        RagdollTypeRegistry.registry(FAIRY_RAGDOLL_TYPE, MAID_FAIRY_PART_BLOCK::get, MAID_FAIRY_PART_BLOCK_ENTITY::get);
        RagdollPartRecognizerRegistry.register(new MaidRagdollPartRecognizer());

        if(CompatMods.LOVE_LOATHE)
            RagdollSaddleLaunch.init();
        if(CompatMods.PLAYER_RAGDOLL)
            PlayerRagdollUtil.init();

        bus.addListener(Network::onRegisterPayloadHandlers);
        bus.addListener(SableMaidRagdollDatagen::onGatherData);
    }

    public static class Network {
        public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
            var registrar = event.registrar(MODID).versioned("2");
            registrar.playToServer(
                    ServerboundEmojiSelectPacket.TYPE,
                    ServerboundEmojiSelectPacket.STREAM_CODEC,
                    ServerboundEmojiSelectPacket::handle
            );

            registrar.playToServer(
                    ServerboundBroomManPacket.TYPE,
                    ServerboundBroomManPacket.STREAM_CODEC,
                    ServerboundBroomManPacket::handle
            );
        }
    }
}
