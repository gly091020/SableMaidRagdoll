package com.gly091020.SableMaidRagdoll.editor;

import com.gly091020.SableMaidRagdoll.init.InitRagdollTypes;
import com.gly091020.SableRagdollLib.editor.api.ModelSceneManager;

public class MaidRagdollEditorRegistry {
    public static void init(){
        ModelSceneManager.registry(InitRagdollTypes.RAGDOLL_TYPE, new MaidModelSceneSupplier());
    }
}
