package com.gly091020.SableMaidRagdoll.editor;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.editor.api.ModelSceneManager;

public class MaidRagdollEditorRegistry {
    public static void init(){
        ModelSceneManager.registry(SableMaidRagdoll.RAGDOLL_TYPE, new MaidModelSceneSupplier());
    }
}
