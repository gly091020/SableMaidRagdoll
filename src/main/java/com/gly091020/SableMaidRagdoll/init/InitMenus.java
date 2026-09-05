package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.menu.MobCannonMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InitMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SableMaidRagdoll.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MobCannonMenu>> MOB_CANNON =
            MENUS.register("mob_cannon", () -> IMenuTypeExtension.create(
                    (id, inventory, data) -> new MobCannonMenu(id, inventory)));

    public static void init(IEventBus bus) {
        MENUS.register(bus);
    }
}
