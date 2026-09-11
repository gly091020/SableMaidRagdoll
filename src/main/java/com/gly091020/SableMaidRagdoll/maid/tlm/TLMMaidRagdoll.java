package com.gly091020.SableMaidRagdoll.maid.tlm;

import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.api.event.MaidAndItemTransformEvent;
import com.github.tartaricacid.touhoulittlemaid.client.sound.data.MaidSoundInstanceAtPos;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.info.models.ServerMaidModels;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.FairyType;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.DefaultMaidSoundPack;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.ItemMaidTooltip;
import com.github.tartaricacid.touhoulittlemaid.inventory.tooltip.YsmMaidInfo;
import com.github.tartaricacid.touhoulittlemaid.network.message.PlayMaidSoundAtPosPackage;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.ChargeSoundManager;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.RagdollBroadcastMessages;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.RagdollSaddleLaunch;
import com.gly091020.SableMaidRagdoll.compat.tlm.MaidRollManager;
import com.gly091020.SableMaidRagdoll.compat.tlm.WineFoxHurtDancingManager;
import com.gly091020.SableMaidRagdoll.init.InitDataComponents;
import com.gly091020.SableMaidRagdoll.maid.api.IMaidRagdoll;
import com.gly091020.SableMaidRagdoll.maid.api.MaidSoundType;
import com.gly091020.SableMaidRagdoll.maid.tlm.block.MaidFairyPartBlockEntity;
import com.gly091020.SableMaidRagdoll.maid.tlm.client.TLMClientEventHandler;
import com.gly091020.SableMaidRagdoll.maid.tlm.editor.MaidRagdollEditorRegistry;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitBlockEntities;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitBlocks;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitItems;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitRagdollTypes;
import com.gly091020.SableMaidRagdoll.maid.tlm.network.ServerboundBroomManPacket;
import com.gly091020.SableMaidRagdoll.maid.tlm.util.TLMMaidCollisionHandler;
import com.gly091020.SableRagdollLib.SableRagdollLib;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.RagdollManager;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.gly091020.SableMaidRagdoll.maid.tlm.TLMEventHandler.BABY_FAIRY;
import static com.gly091020.SableMaidRagdoll.maid.tlm.TLMEventHandler.NEW_FAIRY;

public class TLMMaidRagdoll implements IMaidRagdoll {
    @Override
    public String getID() {
        return "tlm";
    }

    @Override
    public boolean isSupportMaid(Entity entity) {
        return entity instanceof EntityMaid || entity instanceof EntityFairy;
    }

    @Override
    public void init(IEventBus bus) {
        TLMInitBlocks.init();
        TLMInitBlockEntities.init();
        TLMInitItems.init();
        TLMInitRagdollTypes.init();
        NeoForge.EVENT_BUS.register(TLMEventHandler.class);
        if(CompatMods.LOVE_LOATHE.isLoaded())
            RagdollSaddleLaunch.init();
        if(FMLEnvironment.dist.isClient())
            initClient(bus);
    }

    @OnlyIn(Dist.CLIENT)
    public void initClient(IEventBus bus){
        if(SableRagdollLib.hasLDLib())
            MaidRagdollEditorRegistry.init();
        if(CompatMods.LOVE_LOATHE.isLoaded()) {
            ChargeSoundManager.init();
            RagdollBroadcastMessages.init();
        }
        NeoForge.EVENT_BUS.register(TLMClientEventHandler.class);
        bus.addListener(TLMClientEventHandler::onClientSetup);
    }

    @Override
    public ResourceLocation getRagdollId(Entity entity){
        if(entity instanceof EntityFairy fairy)return fairy.isBaby() ? BABY_FAIRY : NEW_FAIRY;
        return entity instanceof EntityMaid maid ? ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, maid.getModelId().replace(":", "/")) : null;
    }

    @Override
    public @Nullable ResourceLocation getRagdollId(MaidDollData data) {
        return ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, data.modelID().replace(":", "/"));
    }

    @Override
    public Ragdoll toMaidRagdoll(ServerLevel level, Entity entity, Vec3 position, Vec3 rotation) {
        var id = getRagdollId(entity);
        if(id == null)return null;
        var rag = RagdollHelper.createRagdoll(level, position, rotation, id);
        if(rag != null && entity instanceof EntityFairy fairy){
            rag.getSublevels().forEach(subLevel -> {
                if(subLevel.getPlot().getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO) instanceof MaidFairyPartBlockEntity blockEntity){
                    blockEntity.setFairyType(FairyType.values()[fairy.getFairyTypeOrdinal()]);
                    blockEntity.setRick(fairy.getName().getString().equals("rick"));
                    blockEntity.setModelType(fairy.isBaby() ? MaidFairyPartBlockEntity.ModelType.BABY : MaidFairyPartBlockEntity.ModelType.NEW);
                }
            });
        }
        return rag;
    }

    @Override
    public void addChatBubble(Entity entity, Component text) {
        if(!(entity instanceof EntityMaid maid))return;
        maid.getChatBubbleManager().addChatBubble(TextChatBubbleData.type2(Component.translatable("text.sablemaidragdoll.please_owner")));
    }

    @Override
    public void playMaidSound(Level level, Vec3 position, String soundPackID, MaidSoundType soundType, float volume) {
        SoundEvent soundEvent = null;
        switch (soundType){
            case HURT -> soundEvent = InitSounds.MAID_HURT.get();
            case IDLE -> soundEvent = InitSounds.MAID_IDLE.get();
        }
        if(soundEvent == null)return;
        if(level.isClientSide)
            playSoundClient(position, soundPackID, soundEvent, volume);
        else PacketDistributor.sendToAllPlayers(new PlayMaidSoundAtPosPackage(
                soundEvent.getLocation(), soundPackID,
                position.x, position.y, position.z, volume, 1
        ));
    }

    @OnlyIn(Dist.CLIENT)
    private void playSoundClient(Vec3 position, String soundPackID, SoundEvent soundEvent, float volume){
        Minecraft.getInstance().getSoundManager().play(new MaidSoundInstanceAtPos(
                soundEvent, soundPackID,
                position.x, position.y, position.z, 0.5f, 1
        ));
    }

    @Override
    public @Nullable Pair<Entity, ItemStack> releaseEntityFromItem(Level level, ItemStack stack) {
        if(stack.is(InitItems.SMART_SLAB_HAS_MAID) || stack.is(InitItems.PHOTO)) {
            CustomData compoundData = stack.get(InitDataComponent.MAID_INFO);
            if (compoundData == null) return null;
            var maid = new EntityMaid(level);
            CompoundTag maidCompound = compoundData.copyTag();
            var event = new MaidAndItemTransformEvent.ToMaid(maid, stack, maidCompound);
            NeoForge.EVENT_BUS.post(event);

            maid.load(maidCompound);
            if (stack.is(InitItems.SMART_SLAB_HAS_MAID))
                return new Pair<>(maid, new ItemStack(InitItems.SMART_SLAB_EMPTY.get(), 1));
            else if (stack.is(InitItems.PHOTO)) {
                stack.shrink(1);
                return new Pair<>(maid, stack);
            }
        }
        return null;
    }

    @Override
    public boolean hasEntity(ItemStack stack) {
        return (stack.is(InitItems.SMART_SLAB_HAS_MAID) || stack.is(InitItems.PHOTO)) && stack.has(InitDataComponent.MAID_INFO);
    }

    @Override
    public boolean isCannonSpecialItem(ItemStack stack) {
        return false;
    }

    @Override
    public void onCannonLaunch(MobCannonBlockEntity blockEntity, Entity entity, Vector3d force) {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Entity getRenderEntity(Level level, MaidDollData data) {
        var modelID = data.modelID();
        if(modelID.isEmpty() || level == null)return null;

        EntityMaid maid;
        try {
            maid = (EntityMaid) EntityCacheUtil.ENTITY_CACHE.get(EntityMaid.TYPE, () -> {
                Entity e = EntityMaid.TYPE.create(level);
                return Objects.requireNonNullElseGet(e, () -> new EntityMaid(level));
            });
        } catch (ExecutionException | ClassCastException ignored) {return null;}
        EntityCacheUtil.clearMaidDataResidue(maid, true);
        maid.setModelId(modelID);
        maid.renderState = MaidRenderState.GARAGE_KIT;
        maid.setInSittingPose(true);
        return maid;
    }

    @Override
    public void appendCommand(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("spawn_test_box").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(TLMMaidRagdoll::spawnTestBox)));
        root.then(Commands.literal("give_garage_kit").then(Commands.argument("entity", EntityArgument.entity()).executes(TLMMaidRagdoll::giveGarageKit)));
        root.then(Commands.literal("spawn_test_maid").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(TLMMaidRagdoll::spawnTestMaid)));
        root.then(Commands.literal("spawn_test_maid_ragdoll").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(TLMMaidRagdoll::spawnTestMaidRagdoll)));
        root.then(Commands.literal("spawn_test_maid_ragdoll_with_entity").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(TLMMaidRagdoll::spawnTestMaidRagdollWithEntity)));
        root.then(Commands.literal("start_roll").then(Commands.argument("entity", EntityArgument.entity()).executes(TLMMaidRagdoll::startRoll)));
        root.then(Commands.literal("start_dance")
                .executes(TLMMaidRagdoll::startDanceAll)
                .then(Commands.argument("entity", EntityArgument.entity()).executes(TLMMaidRagdoll::startDance)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public TooltipComponent getTooltipImage(MaidDollData data) {
        return new ItemMaidTooltip(data.modelID(), "", YsmMaidInfo.EMPTY);
    }

    @Override
    public @Nullable MaidDollData getDataFromEntity(Entity entity) {
        if(!(entity instanceof EntityMaid maid))return null;
        return new MaidDollData(getID(), maid.getModelId(), maid.getSoundPackId(), false);
    }

    @Override
    public void registryNetwork(PayloadRegistrar registrar) {
        registrar.playToServer(
                ServerboundBroomManPacket.TYPE,
                ServerboundBroomManPacket.STREAM_CODEC,
                ServerboundBroomManPacket::handle
        );
    }

    @Override
    public void onPartCollision(Entity entity, BlockPos pos2, AbstractPartBlockEntity blockEntity) {
        TLMMaidCollisionHandler.onCollision(entity, pos2, blockEntity);
    }

    @Override
    public void generateCreateTabDoll(CreativeModeTab.Output output) {
        for (String modelID : ServerMaidModels.getInstance().getModelIdSet()) {
            var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, modelID.replace(":", "/"));
            if(DefFileLoader.getDefFile(ragdollID) == null)continue;
            var stack = new ItemStack(com.gly091020.SableMaidRagdoll.init.InitItems.PLAYER_CHEAT_DEATH_ITEM.get(), 1);
            stack.set(InitDataComponents.MAID_DOLL_DATA, new MaidDollData(getID(), modelID, DefaultMaidSoundPack.DEFAULT_SOUND_PACK_ID, false));
            output.accept(stack);
        }
    }

    @Override
    public void appendCreateTabItem(CreativeModeTab.Output output) {
        if(SableMaidRagdoll.CONFIG.items.spawnEggs){
            output.accept(TLMInitItems.RAGDOLLABLE_MAID_SPAWN_EGG.get());
            output.accept(TLMInitItems.WINE_FOX_SPAWN_EGG.get());
            output.accept(TLMInitItems.RAGDOLLABLE_WINE_FOX_SPAWN_EGG.get());
        }
    }

    public static int giveGarageKit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, "entity");
        var garageKit = new ItemStack(InitItems.GARAGE_KIT.get(), 1);
        var type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        var player = context.getSource().getPlayer();
        if(player == null)return 0;
        CompoundTag data = new CompoundTag();
        data.putString("id", type.toString());
        entity.saveWithoutId(data);
        var customData = CustomData.of(data);

        garageKit.set(InitDataComponent.MAID_INFO, customData);
        player.addItem(garageKit);
        return 1;
    }

    public static int spawnTestBox(CommandContext<CommandSourceStack> context){
        var modelID = ResourceLocationArgument.getId(context, "modelID").toString();
        var world = context.getSource().getLevel();
        var pos = context.getSource().getPosition();

        EntityBox box = new EntityBox(world);
        box.setPos(pos.x, pos.y, pos.z);

        EntityMaid maid = new EntityMaid(world);
        maid.setPos(pos.x, pos.y, pos.z);
        maid.finalizeSpawn(world, world.getCurrentDifficultyAt(BlockPos.containing(pos)), MobSpawnType.SPAWN_EGG, null);
        maid.startRiding(box, true);

        world.tryAddFreshEntityWithPassengers(box);
        maid.setModelId(modelID);
        return 1;
    }

    public static int spawnTestMaid(CommandContext<CommandSourceStack> context) {
        var modelID = ResourceLocationArgument.getId(context, "modelID").toString();
        var maid = new EntityMaid(context.getSource().getLevel());
        maid.setPos(context.getSource().getPosition());
        maid.setHealth(1);
        maid.setModelId(modelID);

        var maxHealth = maid.getAttribute(Attributes.MAX_HEALTH);
        if(maxHealth != null)maxHealth.setBaseValue(1);
        var moveSpeed = maid.getAttribute(Attributes.MOVEMENT_SPEED);
        if(moveSpeed != null)moveSpeed.setBaseValue(0);

        context.getSource().getLevel().addFreshEntity(maid);
        return 1;
    }

    public static int spawnTestMaidRagdoll(CommandContext<CommandSourceStack> context) {
        var modelID = ResourceLocationArgument.getId(context, "modelID").toString();
        var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, modelID.replace(":", "/"));
        if(DefFileLoader.getDefFile(ragdollID) == null)return 0;

        var maid = new EntityMaid(context.getSource().getLevel());
        maid.setPos(context.getSource().getPosition());
        maid.setModelId(modelID);

        var ragdoll = RagdollHelper.createRagdoll(context.getSource().getLevel(), context.getSource().getPosition(), ragdollID);
        if(ragdoll == null)return 0;

        context.getSource().getLevel().addFreshEntity(maid);
        ragdoll.addEntity(maid);
        return 1;
    }

    public static int spawnTestMaidRagdollWithEntity(CommandContext<CommandSourceStack> context) {
        if(context.getSource().getEntity() == null)return 0;
        var modelID = ResourceLocationArgument.getId(context, "modelID").toString();
        var ragdollID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, modelID.replace(":", "/"));
        if(DefFileLoader.getDefFile(ragdollID) == null)return 0;

        var ragdoll = RagdollHelper.createRagdoll(context.getSource().getLevel(), context.getSource().getPosition(), ragdollID);
        if(ragdoll == null)return 0;

        ScheduleManager.scheduleDelayed(context.getSource().getLevel(), 2, () ->
                ragdoll.addEntity(context.getSource().getEntity()));
        return 1;
    }

    public static int startRoll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(!(EntityArgument.getEntity(context, "entity") instanceof EntityMaid maid))
            return 0;
        MaidRollManager.startRolling(maid);
        return 1;
    }

    /** 指定实体对应的布娃娃乱舞（测试用）。 */
    public static int startDance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var entity = EntityArgument.getEntity(context, "entity");
        var ragdoll = RagdollManager.getAll().stream()
                .filter(r -> r.isAlive() && r.getEntity() == entity)
                .findFirst().orElse(null);
        if (ragdoll == null) {
            return 0;
        }
        WineFoxHurtDancingManager.startDancing(ragdoll);
        return 1;
    }

    /** 所有活跃布娃娃乱舞（测试用）。 */
    public static int startDanceAll(CommandContext<CommandSourceStack> context) {
        int count = 0;
        for (var ragdoll : RagdollManager.getAll()) {
            if (!ragdoll.isAlive()) {
                continue;
            }
            WineFoxHurtDancingManager.startDancing(ragdoll);
            count++;
        }
        return count;
    }
}
