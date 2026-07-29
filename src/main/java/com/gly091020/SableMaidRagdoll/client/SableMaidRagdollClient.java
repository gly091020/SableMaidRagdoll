package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.SableMaidRagdollConfig;
import com.gly091020.SableMaidRagdoll.editor.MaidRagdollEditorRegistry;
import com.gly091020.SableRagdollLib.SableRagdollLib;
import com.gly091020.SableRagdollLib.SableRagdollLibConfig;
import com.gly091020.SableRagdollLib.client.button.AllButtons;
import com.gly091020.SableRagdollLib.client.button.Button;
import com.gly091020.SableRagdollLib.client.button.ButtonGuiProvider;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SableMaidRagdoll.MODID, dist = Dist.CLIENT)
public class SableMaidRagdollClient {
    public SableMaidRagdollClient(ModContainer mc){
        if(SableRagdollLib.hasLDLib())
            MaidRagdollEditorRegistry.init();
        mc.registerExtensionPoint(IConfigScreenFactory.class, (m, p) -> AutoConfig.getConfigScreen(SableMaidRagdollConfig.class, p).get());
        AutoConfig.getGuiRegistry(SableMaidRagdollConfig.class).registerAnnotationProvider(new ButtonGuiProvider(), Button.class);
    }
}
