package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.SableMaidRagdollConfig;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.ChargeSoundManager;
import com.gly091020.SableMaidRagdoll.compat.util.CompatMods;
import com.gly091020.SableMaidRagdoll.editor.MaidRagdollEditorRegistry;
import com.gly091020.SableRagdollLib.SableRagdollLib;
import com.gly091020.SableRagdollLib.SableRagdollLibConfig;
import com.gly091020.SableRagdollLib.client.button.AllButtons;
import com.gly091020.SableRagdollLib.client.button.Button;
import com.gly091020.SableRagdollLib.client.button.ButtonGuiProvider;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;

@Mod(value = SableMaidRagdoll.MODID, dist = Dist.CLIENT)
public class SableMaidRagdollClient {
    public static final KeyMapping OPEN_EMOJI = new KeyMapping(
            "key.sablemaidragdoll.open_emoji",
            GLFW.GLFW_KEY_RIGHT_ALT,
            "key.category.sablemaidragdoll"
    );

    public SableMaidRagdollClient(ModContainer mc){
        if(SableRagdollLib.hasLDLib())
            MaidRagdollEditorRegistry.init();
        mc.registerExtensionPoint(IConfigScreenFactory.class, (m, p) -> AutoConfig.getConfigScreen(SableMaidRagdollConfig.class, p).get());
        AutoConfig.getGuiRegistry(SableMaidRagdollConfig.class).registerAnnotationProvider(new ButtonGuiProvider(), Button.class);

        if(CompatMods.LOVE_LOATHE)
            ChargeSoundManager.init();
    }

    @EventBusSubscriber(modid = SableMaidRagdoll.MODID, value = Dist.CLIENT)
    public static class EventHandler {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_EMOJI);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            while (OPEN_EMOJI.consumeClick()) {
                EmojiSelectScreen.tryOpen();
            }
        }
    }
}
