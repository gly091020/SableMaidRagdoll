package com.gly091020.SableMaidRagdoll.init;

import com.github.tartaricacid.touhoulittlemaid.init.InitCreativeTabs;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MaidCreativeTab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.gly091020.SableMaidRagdoll.SableMaidRagdoll.CONFIG;

public class InitCreativeModeTab {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SableMaidRagdoll.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            r -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sable_maid_ragdoll.main"))
                    .icon(() -> InitItems.MOD_ICON_ITEM.get().getDefaultInstance())
                    .withTabsBefore(InitCreativeTabs.MAIN_TAB.getId())
                    .displayItems(MaidCreativeTab::getAllMainItem)
                    .build());
    public static DeferredHolder<CreativeModeTab, CreativeModeTab> DOLL_TAB;

    public static void init(IEventBus bus){
        if(CONFIG.items.enableDollTab)
            InitCreativeModeTab.DOLL_TAB = MaidCreativeTab.createDollTab(InitCreativeModeTab.CREATIVE_TABS);
        CREATIVE_TABS.register(bus);
    }
}
