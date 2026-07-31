package com.gly091020.SableMaidRagdoll.compat.love_loathe;

import com.github.tartaricacid.callresponse.compat.emotion.SaddleChargeHandler;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChargeSoundManager {
    public static SoundInstance bigDog;
    public static void init(){
        SaddleChargeHandler.setCallback(ChargeSoundManager::call);
    }

    public static void call(boolean pressed){
        if(!SableMaidRagdoll.CONFIG.sounds.bigDog)return;
        if(pressed) {
            bigDog = SimpleSoundInstance.forUI(SableMaidRagdoll.BIG_DOG, 1);
            Minecraft.getInstance().getSoundManager().play(bigDog);
        }else {
            if(bigDog != null)
                Minecraft.getInstance().getSoundManager().stop(bigDog);
            bigDog = null;
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SableMaidRagdoll.DOG_CALL, 1));
        }
    }
}
