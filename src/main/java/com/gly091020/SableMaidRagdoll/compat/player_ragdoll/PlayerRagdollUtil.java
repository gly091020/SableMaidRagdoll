package com.gly091020.SableMaidRagdoll.compat.player_ragdoll;

import dev.leo.sableplayerragdoll.api.RagdollAPI;
import dev.leo.sableplayerragdoll.mob.api.MobRagdollLaunchOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class PlayerRagdollUtil {
    public static void launch(ServerPlayer player, Vec3 vec3){
        RagdollAPI.launch(player, vec3);
    }

    public static void launchMob(LivingEntity mob, Vec3 vec3){
        RagdollAPI.launchMob((ServerLevel) mob.level(), mob, vec3, Vec3.ZERO, new MobRagdollLaunchOptions(Integer.MAX_VALUE));
    }

    public static void init(){
        NeoForge.EVENT_BUS.register(PlayerRagdollEventHandler.class);
    }
}
