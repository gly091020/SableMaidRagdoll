package com.gly091020.SableMaidRagdoll.editor;

import com.gly091020.SableMaidRagdoll.editor.editor.MaidRagdollEditor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import net.minecraft.client.Minecraft;

public class EditorOpener {
    public static void openEditor(){
        var window = EditorWindow.open(MaidRagdollEditor.WINDOW_ID, MaidRagdollEditor::new);
        var ui = new ModularUI(UI.of(window))
                .shouldCloseOnEsc(false)
                .shouldCloseOnKeyInventory(false);
        Minecraft.getInstance().setScreen(new ModularUIScreen(ui, window.getEditorName()));
    }
}
