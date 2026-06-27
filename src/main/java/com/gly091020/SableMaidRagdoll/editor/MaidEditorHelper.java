package com.gly091020.SableMaidRagdoll.editor;

import com.github.tartaricacid.touhoulittlemaid.client.resource.models.MaidModels;
import com.gly091020.SableMaidRagdoll.editor.sceneObject.MaidPartObject;
import com.lowdragmc.lowdraglib2.editor.ui.sceneeditor.sceneobject.SceneObject;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3f;

public class MaidEditorHelper {
    public static SceneObject buildModelSceneObjects(String modelName) {
        var model = MaidModels.getInstance().getModel(modelName).orElse(null);
        var info = MaidModels.getInstance().getInfo(modelName).orElse(null);
        if (model == null || info == null) return null;

        var root = new SceneObject();
        model.getModelMap().forEach((name, part) -> {
            var p = new MaidPartObject(modelName, name, Shapes.block());
            p.transform().parent(root.transform());
        });
        root.transform().rotate(new Vector3f(0, 0, 1), (float) Math.PI);
        root.transform().rotate(new Vector3f(0, 1, 0), (float) Math.PI / 4 * -1);
        return root;
    }
}
