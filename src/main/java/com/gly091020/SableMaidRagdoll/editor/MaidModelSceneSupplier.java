package com.gly091020.SableMaidRagdoll.editor;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.models.MaidModels;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MixinFunction;
import com.gly091020.SableRagdollLib.editor.api.IModelSceneSupplier;
import com.gly091020.SableRagdollLib.editor.api.AbstractModelSceneObject;
import com.lowdragmc.lowdraglib2.gui.util.TreeNode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class MaidModelSceneSupplier implements IModelSceneSupplier {
    @Override
    public ResourceLocation getModel(Path path) {
        var id = toRL(path);
        if(id == null)return null;
        if(MaidModels.getInstance().getModel(id.toString()).isEmpty())return null;
        return id;
    }

    public static ResourceLocation toRL(Path path) {
        String marker = SableMaidRagdoll.MODID;
        int count = path.getNameCount();
        int index = -1;
        for (int i = 0; i < count; i++) {
            if (path.getName(i).toString().equals(marker)) {
                index = i;
                break;
            }
        }
        if (index == -1 || index + 2 >= count) {
            return null;
        }
        String namespace = path.getName(index + 1).toString();
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(".json")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        return ResourceLocation.fromNamespaceAndPath(namespace, fileName);
    }

    @Override
    public void buildModelTree(ResourceLocation resourceLocation, TreeNode<String, String> root) {
        var model = MaidModels.getInstance().getModel(resourceLocation.toString()).orElse(null);
        if(model == null)return;

        for (BedrockPart part : MixinFunction.getShouldRender(model)){
            addChild(model, part, root);
        }
    }

    private void addChild(BedrockModel<?> model, BedrockPart bedrockPart, TreeNode<String, String> root){
        var name = getBedrockPartName(model, bedrockPart);
        if(name == null)return;
        var node = root.createChild(name);
        for (BedrockPart part: bedrockPart.children){
            addChild(model, part, node);
        }
    }

    @Nullable
    private String getBedrockPartName(BedrockModel<?> model, BedrockPart part){
        var index = model.getModelMap().values().stream().toList().indexOf(part);
        if(index == -1)return null;
        return model.getModelMap().keySet().stream().toList().get(index);
    }

    @Override
    public AbstractModelSceneObject createNewModelObject(ResourceLocation resourceLocation) {
        return new MaidModelObject(resourceLocation);
    }
}
