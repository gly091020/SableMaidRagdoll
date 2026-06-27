package com.gly091020.SableMaidRagdoll.editor.editor;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.editor.view.MaidModelView;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MaidRagdollEditor extends Editor {
    public static final ResourceLocation WINDOW_ID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "editor");

    public MaidModelView maidModelView;

    @Override
    protected @NotNull Editor createNewEditorInstance() {
        return new MaidRagdollEditor();
    }

    @Override
    protected void initMenus() {
        super.initMenus();
        fileMenu.addProjectProvider(MaidRagdollProjectType.TYPE);
        maidModelView = new MaidModelView(this);
        placeView(maidModelView, () -> bottomWindow.getLeftBottom());
    }

    @Override
    protected void onPrepareResourceView() {

    }
}
