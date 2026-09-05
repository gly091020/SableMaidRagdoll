package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.SableMaidRagdollConfig;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.ChargeSoundManager;
import com.gly091020.SableMaidRagdoll.compat.love_loathe.RagdollBroadcastMessages;
import com.gly091020.SableMaidRagdoll.compat.util.CompatMods;
import com.gly091020.SableMaidRagdoll.editor.MaidRagdollEditorRegistry;
import com.gly091020.SableRagdollLib.SableRagdollLib;
import com.gly091020.SableRagdollLib.client.button.Button;
import com.gly091020.SableRagdollLib.client.button.ButtonGuiProvider;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;

@Mod(value = SableMaidRagdoll.MODID, dist = Dist.CLIENT)
public class SableMaidRagdollClient {
    public static final KeyMapping OPEN_EMOJI = new KeyMapping(
            "key.sablemaidragdoll.open_emoji",
            GLFW.GLFW_KEY_RIGHT_ALT,
            "key.category.sablemaidragdoll"
    );
    public static final KeyMapping AIM_CANNON = new KeyMapping(
            "key.sablemaidragdoll.aim_cannon",
            GLFW.GLFW_KEY_G,
            "key.category.sablemaidragdoll"
    );

    public SableMaidRagdollClient(ModContainer mc){
        if(SableRagdollLib.hasLDLib())
            MaidRagdollEditorRegistry.init();
        mc.registerExtensionPoint(IConfigScreenFactory.class, (m, p) -> AutoConfig.getConfigScreen(SableMaidRagdollConfig.class, p).get());
        AutoConfig.getGuiRegistry(SableMaidRagdollConfig.class).registerAnnotationProvider(new ButtonGuiProvider(), Button.class);

        if(CompatMods.LOVE_LOATHE) {
            ChargeSoundManager.init();
            RagdollBroadcastMessages.init();
        }
    }
}
