package com.gly091020.SableMaidRagdoll.maid.lmrb;

import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.gly091020.SableMaidRagdoll.block.mob_cannon.MobCannonBlockEntity;
import com.gly091020.SableMaidRagdoll.maid.api.IMaidRagdoll;
import com.gly091020.SableMaidRagdoll.maid.api.MaidSoundType;
import com.gly091020.SableMaidRagdoll.maid.lmrb.block.LittleMaidPartBlockEntity;
import com.gly091020.SableMaidRagdoll.maid.lmrb.client.LMRBClientEventHandler;
import com.gly091020.SableMaidRagdoll.maid.lmrb.client.LMRBMaidRenderCache;
import com.gly091020.SableMaidRagdoll.maid.lmrb.client.LMRBSoundPlayer;
import com.gly091020.SableMaidRagdoll.maid.lmrb.init.LMRBInitBlockEntities;
import com.gly091020.SableMaidRagdoll.maid.lmrb.init.LMRBInitBlocks;
import com.gly091020.SableMaidRagdoll.maid.lmrb.init.LMRBInitRagdollTypes;
import com.gly091020.SableMaidRagdoll.maid.lmrb.network.ClientboundMaidSoundPacket;
import com.gly091020.SableRagdollLib.api.Ragdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.block.AbstractPartBlockEntity;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.resource.holder.TextureHolder;
import net.sistr.littlemaidmodelloader.resource.util.LMSounds;
import net.sistr.littlemaidrebirth.LMRBMod;
import net.sistr.littlemaidrebirth.entity.LittleMaidEntity;
import net.sistr.littlemaidrebirth.setup.Registration;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.Locale;

/**
 * LittleMaidRebirth（LMML 模型）女仆的布娃娃实现。
 * <p>
 * 布娃娃定义按 LMML 的模型名选择（{@code data/littlemaidrebirth/ragdoll/<模型名>.json}），
 * 创建时把女仆的贴图信息写入各部位方块实体，客户端渲染时据此解析贴图。
 */
public class LMRBMaidRagdoll implements IMaidRagdoll {
    public static final String ID = LMRBMod.MODID;

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public boolean isSupportMaid(Entity entity) {
        return entity instanceof LittleMaidEntity;
    }

    @Override
    public void init(IEventBus bus) {
        LMRBInitBlocks.init();
        LMRBInitBlockEntities.init();
        LMRBInitRagdollTypes.init();
        if (FMLEnvironment.dist.isClient())
            initClient(bus);
    }

    @OnlyIn(Dist.CLIENT)
    private void initClient(IEventBus bus) {
        NeoForge.EVENT_BUS.register(LMRBClientEventHandler.class);
        bus.addListener(LMRBClientEventHandler::onClientSetup);
    }

    @Override
    public @Nullable ResourceLocation getRagdollId(Entity entity) {
        if (!(entity instanceof LittleMaidEntity maid))
            return null;
        var modelName = getModelName(maid);
        return DefFileLoader.getDefFile(ResourceLocation.fromNamespaceAndPath(ID, modelName)) == null
                ? null
                : ResourceLocation.fromNamespaceAndPath(ID, modelName);
    }

    @Override
    public @Nullable ResourceLocation getRagdollId(MaidDollData data) {
        if (data.modelID().isEmpty())
            return null;
        return ResourceLocation.fromNamespaceAndPath(ID, data.modelID().toLowerCase(Locale.ROOT));
    }

    @Override
    public @Nullable Ragdoll toMaidRagdoll(ServerLevel level, Entity entity, Vec3 position, Vec3 rotation) {
        if (!(entity instanceof LittleMaidEntity maid))
            return null;
        var id = getRagdollId(entity);
        if (id == null)
            return null;
        var ragdoll = RagdollHelper.createRagdoll(level, position, rotation, id);
        if (ragdoll == null)
            return null;
        applyTexture(ragdoll, maid);
        // 死亡流程里女仆自己的 se_death 还会响，这里只处理被打飞等实体还活着的情况
        if (maid.isAlive())
            playMaidSound(level, position, maid.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD).getTextureName(),
                    MaidSoundType.HURT, 1.0F);
        return ragdoll;
    }

    /** 把女仆当前的贴图信息写到布娃娃的所有部位方块实体上。 */
    public static void applyTexture(Ragdoll ragdoll, LittleMaidEntity maid) {
        TextureHolder holder = maid.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD);
        int color = maid.getColorMM().getIndex();
        boolean contract = maid.isContractMM();
        ragdoll.getSublevels().forEach(subLevel -> {
            if (subLevel.getPlot().getEmbeddedLevelAccessor().getBlockEntity(BlockPos.ZERO) instanceof LittleMaidPartBlockEntity blockEntity) {
                blockEntity.setMaidTexture(holder.getTextureName(), color, contract);
            }
        });
    }

    public static String getModelName(LittleMaidEntity maid) {
        return maid.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD)
                .getModelName().toLowerCase(Locale.ROOT);
    }

    @Override
    public void addChatBubble(Entity entity, Component text) {
    }

    @Override
    public void playMaidSound(Level level, Vec3 position, String soundPackID, MaidSoundType soundType, float volume) {
        String soundName = switch (soundType) {
            case HURT -> LMSounds.HURT;
            case IDLE -> LMSounds.LIVING_DAYTIME;
        };
        if (level.isClientSide) {
            playSoundClient(position, soundPackID, soundName, volume);
        } else if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null,
                    position.x, position.y, position.z, 64.0D,
                    new ClientboundMaidSoundPacket(soundPackID, soundName, position, volume));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playSoundClient(Vec3 position, String soundPackID, String soundName, float volume) {
        LMRBSoundPlayer.play(soundPackID, soundName, position, volume);
    }

    @Override
    public @Nullable Pair<Entity, ItemStack> releaseEntityFromItem(Level level, ItemStack stack) {
        return null;
    }

    @Override
    public boolean hasEntity(ItemStack stack) {
        return false;
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
    public @Nullable Entity getRenderEntity(Level level, MaidDollData data) {
        return LMRBMaidRenderCache.get(level, data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @Nullable TooltipComponent getTooltipImage(MaidDollData data) {
        return null;
    }

    @Override
    public @Nullable MaidDollData getDataFromEntity(Entity entity) {
        if(!(entity instanceof LittleMaidEntity littleMaidEntity))return null;
        return new MaidDollData(ID, getModelName(littleMaidEntity), littleMaidEntity.soundPlayer.getConfigHolder().getPackName(), false);
    }

    @Override
    public void registryNetwork(PayloadRegistrar registrar) {
        registrar.playToClient(
                ClientboundMaidSoundPacket.TYPE,
                ClientboundMaidSoundPacket.STREAM_CODEC,
                ClientboundMaidSoundPacket::handle
        );
    }

    @Override
    public void onPartCollision(Entity entity, BlockPos pos2, AbstractPartBlockEntity blockEntity) {
    }

    @Override
    public void generateCreateTabDoll(CreativeModeTab.Output output) {
    }

    @Override
    public void appendCreateTabItem(CreativeModeTab.Output output) {
    }

    @Override
    public void appendCommand(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("spawn_lmrb_ragdoll").executes(LMRBMaidRagdoll::spawnTestRagdoll));
    }

    /** 生成一只随机贴图的女仆并直接布娃娃化（测试用）。 */
    public static int spawnTestRagdoll(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        ServerLevel level = source.getLevel();
        var pos = source.getPosition();
        var maid = Registration.LITTLE_MAID_MOB.get().spawn(level, BlockPos.containing(pos), MobSpawnType.COMMAND);
        if (maid == null)
            return 0;
        maid.setPos(pos);
        maid.setRandomTexture();

        var id = new LMRBMaidRagdoll().getRagdollId(maid);
        if (id == null) {
            maid.discard();
            return 0;
        }
        var ragdoll = RagdollHelper.createRagdoll(level, maid.position(), new Vec3(0, -maid.getYHeadRot(), 0), id);
        if (ragdoll == null) {
            maid.discard();
            return 0;
        }
        applyTexture(ragdoll, maid);
        ragdoll.addEntity(maid);
        return 1;
    }
}
