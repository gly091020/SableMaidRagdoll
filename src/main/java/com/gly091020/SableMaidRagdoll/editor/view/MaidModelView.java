package com.gly091020.SableMaidRagdoll.editor.view;

import com.github.tartaricacid.touhoulittlemaid.client.resource.models.MaidModels;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.CustomModelPack;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.gly091020.SableMaidRagdoll.editor.element.MaidModelElement;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Tab;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TabView;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import org.appliedenergistics.yoga.YogaOverflow;

public class MaidModelView extends View {
    public final TabView mainTab = new TabView();

    public MaidModelView(Editor editor) {
        super("text.sablemaidragdoll.model_view");

        getLayout().flexDirection(FlexDirection.COLUMN);

        mainTab.layout(l -> {
            l.flex(1);
            l.widthPercent(100);
            l.heightPercent(100);
        }).moveInlineAsDefault();

        mainTab.tabContentContainer.layout(l -> {
            l.flex(1);
            l.paddingAll(2);
        }).moveInlineAsDefault();

        mainTab.tabHeaderContainer.layout(l -> {
            l.flexDirection(FlexDirection.ROW);
        }).moveInlineAsDefault();

        fillMainTab();

        addChild(mainTab);
    }

    private void fillMainTab() {
        for (CustomModelPack<MaidModelInfo> pack : MaidModels.getInstance().getPackList()) {
            var tab = new Tab();
            tab.setText(MaidModelHelper.paste943String(pack.getPackName()));

            var scrollerView = new ScrollerView();
            scrollerView.layout(l -> {
                l.flex(1);
                l.widthPercent(100);
                l.heightPercent(100);
            });
            scrollerView.scrollerStyle(scrollerViewStyle ->
                    scrollerViewStyle.maxScrollPixel(20));

            var content = new UIElement();
            content.layout(l -> {
                l.display(TaffyDisplay.FLEX);
                l.flexDirection(FlexDirection.ROW);
                l.flexWrap(FlexWrap.WRAP);
                l.alignContent(AlignContent.FLEX_START);
                l.widthPercent(100);
            });

            for (MaidModelInfo maidModelInfo : pack.getModelList()) {
                var maidModel = new MaidModelElement(
                        maidModelInfo.getModelId().toString()
                );

                maidModel.layout(l -> {
                    l.width(60f);
                    l.height(60f);
                    l.marginAll(2);
                    l.overflow(YogaOverflow.HIDDEN);
                });

                content.addChild(maidModel);
            }
            scrollerView.addScrollViewChild(content);
            mainTab.addTab(tab, scrollerView);
        }
    }
}