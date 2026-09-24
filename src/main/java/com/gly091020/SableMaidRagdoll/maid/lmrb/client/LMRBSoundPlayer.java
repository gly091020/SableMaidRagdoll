package com.gly091020.SableMaidRagdoll.maid.lmrb.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sistr.littlemaidmodelloader.LMMLMod;
import net.sistr.littlemaidmodelloader.client.resource.LMSoundInstance;
import net.sistr.littlemaidmodelloader.client.resource.manager.LMSoundManager;
import net.sistr.littlemaidmodelloader.resource.holder.ConfigHolder;
import net.sistr.littlemaidmodelloader.resource.manager.LMConfigManager;

import java.util.Locale;

/**
 * 无实体播放 LMML 声音包音效：按声音包名取配置，解析音效名后在坐标处播放。
 * 复刻 {@code SoundPlayableCompound.play} 的客户端逻辑，但不依赖任何实体。
 */
@OnlyIn(Dist.CLIENT)
public final class LMRBSoundPlayer {
    private LMRBSoundPlayer() {
    }

    public static void play(String soundPack, String soundName, Vec3 pos, float volume) {
        ConfigHolder config = LMConfigManager.INSTANCE.getConfig(soundPack)
                .orElseGet(LMConfigManager.INSTANCE::getAnyConfig);
        config.getSoundFileName(soundName.toLowerCase(Locale.ROOT)).ifPresent(soundFile ->
                LMSoundManager.INSTANCE.getSound(soundFile).ifPresent(soundSet ->
                        Minecraft.getInstance().getSoundManager().play(new LMSoundInstance(
                                soundSet,
                                SoundSource.NEUTRAL,
                                volume * LMMLMod.getConfig().getVoiceVolume(),
                                pos.x, pos.y, pos.z
                        ))));
    }
}
