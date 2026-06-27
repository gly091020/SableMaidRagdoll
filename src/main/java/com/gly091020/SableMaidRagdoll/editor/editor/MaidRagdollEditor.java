package com.gly091020.SableMaidRagdoll.editor.editor;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.editor.MaidEditorHelper;
import com.gly091020.SableMaidRagdoll.editor.view.MaidModelView;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.editor.ui.sceneeditor.SceneEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MaidRagdollEditor extends Editor {
    public static final ResourceLocation WINDOW_ID = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "editor");

    public MaidModelView maidModelView;
    public SceneEditor sceneEditor;

    @Override
    protected @NotNull Editor createNewEditorInstance() {
        return new MaidRagdollEditor();
    }

    @Override
    protected void initMenus() {
        super.initMenus();
        fileMenu.addProjectProvider(MaidRagdollProjectType.TYPE);
        maidModelView = new MaidModelView(this);
        sceneEditor = new SceneEditor();
        sceneEditor.layout(l -> {
            l.widthPercent(100);
            l.flex(1);
        });
        sceneEditor.scene
                .createScene(Objects.requireNonNull(Minecraft.getInstance().level))
                .setRenderedCore(List.of(BlockPos.ZERO.below(2)))
                .useCacheBuffer();
        if (sceneEditor.scene.getDummyWorld() != null) {
            sceneEditor.scene.getDummyWorld().setBlock(BlockPos.ZERO.below(2), Blocks.AIR.defaultBlockState(), 3);
        }

        var obj = MaidEditorHelper.buildModelSceneObjects("authors_and_credits:wine_fox_taisho");
        if(obj != null)
            obj.setScene(sceneEditor);

        placeView(maidModelView, () -> bottomWindow.getLeftBottom());
        var view = new View(Component.translatable("text.sablemaidragdoll.model_preview").getString());
        view.addChildren(sceneEditor);
        centerWindow.getLeftTop().addView(view);
    }

    @Override
    protected void onPrepareResourceView() {

    }
}
