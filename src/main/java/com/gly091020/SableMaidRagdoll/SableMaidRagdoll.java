package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableMaidRagdoll.compat.love_loathe.RagdollSaddleLaunch;
import com.gly091020.SableMaidRagdoll.compat.player_ragdoll.PlayerRagdollUtil;
import com.gly091020.SableMaidRagdoll.compat.util.CompatMods;
import com.gly091020.SableMaidRagdoll.datagen.SableMaidRagdollDatagen;
import com.gly091020.SableMaidRagdoll.init.*;
import com.gly091020.SableMaidRagdoll.network.PacketRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(SableMaidRagdoll.MODID)
public class SableMaidRagdoll {
    public static final String MODID = "sablemaidragdoll";
    public static SableMaidRagdollConfig CONFIG;

    public SableMaidRagdoll(IEventBus bus){
        CONFIG = AutoConfig.register(SableMaidRagdollConfig.class, Toml4jConfigSerializer::new).getConfig();

        InitItems.init(bus);
        InitBlocks.init(bus);
        InitBlockEntities.init(bus);
        InitMenus.init(bus);
        InitCapabilities.init(bus);
        InitSounds.init(bus);
        InitDataComponents.init(bus);
        InitAttachmentTypes.init(bus);
        InitCustomStats.init(bus);
        InitCreativeModeTab.init(bus);
        InitRagdollTypes.init();

        if(CompatMods.LOVE_LOATHE)
            RagdollSaddleLaunch.init();
        if(CompatMods.PLAYER_RAGDOLL)
            PlayerRagdollUtil.init();

        bus.addListener(PacketRegistry::onRegisterPayloadHandlers);
        bus.addListener(SableMaidRagdollDatagen::onGatherData);
    }
}
