package com.gly091020.SableMaidRagdoll.compat.love_loathe;

import com.github.JumDa5he.callresponse.compat.api.event.saddle.SaddleEvent;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.util.MixinFunction;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class RagdollSaddleLaunch {
    public static void init(){
        NeoForge.EVENT_BUS.register(RagdollSaddleLaunch.class);
    }

    @SubscribeEvent
    public static void onLaunch(SaddleEvent.Launch.AfterPush event){
        if(!SableMaidRagdoll.CONFIG.loveAndLoathe.drop)return;
        var maid = event.getMaid();
        var player = event.getPlayer();
        var chargePercent = event.getChargePrecent();
        var level = maid.level();
        if(level.isClientSide)return;
        var m = JOMLConversion.toJOML(maid.getDeltaMovement()).mul(5);
        if(chargePercent > 0)
            m.sub(0, 0.8, 0);
        MixinFunction.saddleLaunchCreateRagdoll((ServerLevel) level, maid, m, true);
        if(SableMaidRagdoll.CONFIG.sounds.drop && chargePercent == 0)
            player.level().playSound(null, BlockPos.containing(player.position()), InitSounds.DROP.get(), SoundSource.PLAYERS, 1, 1);
    }
}
