package com.gly091020.SableMaidRagdoll.editor.editor;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.mojang.serialization.JsonOps;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Supplier;

public class MaidRagdollProjectType extends ProjectType {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    public static final MaidRagdollProjectType TYPE = new MaidRagdollProjectType(
            Icons.FILE,
            "ragdoll.json",
            ".json",
            MaidRagdollProject::new
    );

    private MaidRagdollProjectType(IGuiTexture icon, String name, String suffix, Supplier<IProject> projectCreator) {
        super(icon, name, suffix, projectCreator);
    }

    @Override
    public IProject loadProjectFromFile(File file) throws Exception {
        var instance = new MaidRagdollProject();
        var p = MaidPartDefFileLoader.DefFile.CODEC.parse(JsonOps.INSTANCE,
                        GSON.fromJson(Files.readString(file.toPath()),
                                JsonElement.class))
                .resultOrPartial(e -> SableMaidRagdoll.LOGGER.error("加载时出现错误：{}", e));
        p.ifPresent(defFile -> instance.file = defFile);
        return instance;
    }

    @Override
    public void saveProjectToFile(IProject project, File file) throws Exception {
        if(!(project instanceof MaidRagdollProject maidRagdollProject))throw new RuntimeException();
        var r = MaidPartDefFileLoader.DefFile.CODEC.encodeStart(JsonOps.INSTANCE, maidRagdollProject.file)
                .resultOrPartial(e -> SableMaidRagdoll.LOGGER.error("保存时出现错误：{}", e));
        if(r.isPresent())
            Files.writeString(file.toPath(), GSON.toJson(r.get()));
    }

    @Override
    public boolean isProjectDirty(IProject project, File file) throws Exception {
        if(!(project instanceof MaidRagdollProject maidRagdollProject))return false;
        var r = MaidPartDefFileLoader.DefFile.CODEC.encodeStart(JsonOps.INSTANCE, maidRagdollProject.file)
                .resultOrPartial(e -> SableMaidRagdoll.LOGGER.error("读取时出现错误：{}", e));
        if(r.isEmpty())return true;
        return GSON.toJson(r).equals(Files.readString(file.toPath()));
    }
}
