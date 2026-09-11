package com.gly091020.SableMaidRagdoll.maid.tlm.editor;

import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitRagdollTypes;
import com.gly091020.SableRagdollLib.editor.api.ModelSceneManager;

public class MaidRagdollEditorRegistry {
    public static void init(){
        ModelSceneManager.registry(TLMInitRagdollTypes.RAGDOLL_TYPE, new MaidModelSceneSupplier());
    }
}
