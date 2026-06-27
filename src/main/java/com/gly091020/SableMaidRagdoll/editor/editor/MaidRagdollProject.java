package com.gly091020.SableMaidRagdoll.editor.editor;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MaidPartDefFileLoader;
import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MaidRagdollProject implements IProject {
    private final Resources resources = Resources.of();

    public MaidPartDefFileLoader.DefFile file;
    @Override
    public Resources getResources() {
        return resources;
    }

    @Override
    public ProjectType getProjectType() {
        return MaidRagdollProjectType.TYPE;
    }

    @Override
    public void initNewProject() {
        file = new MaidPartDefFileLoader.DefFile(
                Map.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    @Override
    public CompoundTag serializeProject(@NotNull HolderLookup.Provider provider) {
        if(file == null)return new CompoundTag();
        var r = MaidPartDefFileLoader.DefFile.CODEC.encodeStart(NbtOps.INSTANCE, file)
                .resultOrPartial(e -> SableMaidRagdoll.LOGGER.error("保存时出现错误：{}", e));
        return r.map(tag -> (CompoundTag) tag).orElseGet(CompoundTag::new);
    }

    @Override
    public void deserializeProject(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag nbt) {
        MaidPartDefFileLoader.DefFile.CODEC.parse(NbtOps.INSTANCE, nbt)
                .resultOrPartial(e -> SableMaidRagdoll.LOGGER.error("加载时出现错误：{}", e))
                .ifPresent(defFile -> file = defFile);
    }
}
