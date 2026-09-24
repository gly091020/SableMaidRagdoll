package com.gly091020.SableMaidRagdoll.compat.revive_maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.DefaultMaidSoundPack;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.gly091020.SableMaidRagdoll.init.InitTags;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableRagdollLib.api.event.SwitchControlModeEvent;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class ReviveMaidEventHandler {
    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event){
        if(!SableMaidRagdoll.CONFIG.ragdollOnSpecialDamage)return;
        if(!(event.getSource().is(InitTags.ALWAYS_TO_RAGDOLL_TAG)))return;
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        if(player.getVehicle() instanceof PartSeat)return;
        var position = event.getSource().getSourcePosition();
        Vec3 direction = player
                .position()
                .subtract(position == null ? Vec3.ZERO : position)
                .normalize();
        var motion = direction
                .scale(5)
                .add(0, 1, 0);
        createRagdoll(player, motion);
    }

    private static void createRagdoll(ServerPlayer player, Vec3 motion){
        var modelID = ReviveMaidUtil.getPlayerPossessionTarget(player);
        if(modelID == null)return;
        var soundID = DefaultMaidSoundPack.getInitSoundPackId();
        var ragdoll = MaidRagdollTypesManager.createRagdollFromDoll(player, player.position().add(0, 0.5, 0), new Vec3(0, -player.getYHeadRot(), 0), motion, Vec3.ZERO, true, new MaidDollData(
                "tlm", modelID, soundID
        ));
        if(ragdoll == null)return;
        ragdoll.getExtraData().putString("PCDI_soundID", soundID);
        ragdoll.getExtraData().putString("PCDI_typeID", "tlm");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if(event.getEntity().getVehicle() instanceof PartSeat)event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onSwitchMaid(SwitchControlModeEvent event){
        var player = event.getServerPlayer();
        if(player.getVehicle() instanceof PartSeat)return;
        if(ReviveMaidUtil.getPlayerPossessionTarget(player) == null)return;
        createRagdoll(player, Vec3.ZERO);
        event.setCanceled(true);
    }
}
