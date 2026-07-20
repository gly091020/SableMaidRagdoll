package com.gly091020.SableMaidRagdoll.client;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.editor.MaidRagdollEditorRegistry;
import com.gly091020.SableRagdollLib.SableRagdollLib;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = SableMaidRagdoll.MODID, dist = Dist.CLIENT)
public class SableMaidRagdollClient {
    public SableMaidRagdollClient(){
        if(SableRagdollLib.hasLDLib())
            MaidRagdollEditorRegistry.init();
    }
}
