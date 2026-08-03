package com.gly091020.SableMaidRagdoll.compat.love_loathe;

import com.github.tartaricacid.callresponse.compat.api.event.saddle.SaddleEvent;
import com.github.tartaricacid.callresponse.compat.emotion.SaddleChargeHandler;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

@OnlyIn(Dist.CLIENT)
public class ChargeSoundManager {
    public static SoundInstance bigDog;
    public static void init(){
        NeoForge.EVENT_BUS.register(ChargeSoundManager.class);
    }

    @SubscribeEvent
    public static void onStart(SaddleEvent.Charge.Start event){
        if(!SableMaidRagdoll.CONFIG.sounds.bigDog)return;
        bigDog = SimpleSoundInstance.forUI(SableMaidRagdoll.BIG_DOG, 1);
        Minecraft.getInstance().getSoundManager().play(bigDog);
    }

    @SubscribeEvent
    public static void onEnd(SaddleEvent.Charge.End event){
        if(!SableMaidRagdoll.CONFIG.sounds.bigDog)return;
        if(bigDog != null)
            Minecraft.getInstance().getSoundManager().stop(bigDog);
        bigDog = null;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SableMaidRagdoll.DOG_CALL, 1));
    }
}
