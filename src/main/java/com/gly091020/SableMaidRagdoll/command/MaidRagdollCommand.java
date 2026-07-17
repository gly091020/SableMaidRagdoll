package com.gly091020.SableMaidRagdoll.command;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.api.RagdollHelper;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class MaidRagdollCommand {
    public static final String COMMAND = "sable_maid_ragdoll";
    public static void registry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal(COMMAND);
        root.requires(source -> source.hasPermission(2));
        root.then(Commands.literal("spawn_test_maid").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(MaidRagdollCommand::spawnTestMaid)));
        root.then(Commands.literal("spawn_test_maid_ragdoll").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(MaidRagdollCommand::spawnTestMaidRagdoll)));
        root.then(Commands.literal("spawn_test_maid_ragdoll_with_entity").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(MaidRagdollCommand::spawnTestMaidRagdollWithEntity)));
        root.then(Commands.literal("spawn_test_box").then(Commands.argument("modelID", ResourceLocationArgument.id()).executes(MaidRagdollCommand::spawnTestBox)));
        root.then(Commands.literal("give_garage_kit").then(Commands.argument("entity", EntityArgument.entity()).executes(MaidRagdollCommand::giveGarageKit)));
        dispatcher.register(root);
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
}
