package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, SableMaidRagdoll.MODID, helper);
    }

    @Override
    protected void registerModels() {
        basicItem(SableMaidRagdoll.CHEAT_DEATH_BAUBLE_ITEM.get());
        basicItem(SableMaidRagdoll.MAID_MACE_ITEM.get());
        basicItem(SableMaidRagdoll.MOD_ICON_ITEM.get());
        basicItem(SableMaidRagdoll.SONIC_WAVE_ITEM.get());
        spawnEggItem(SableMaidRagdoll.RAGDOLLABLE_MAID_SPAWN_EGG.get());
        spawnEggItem(SableMaidRagdoll.WINE_FOX_SPAWN_EGG.get());
        simpleBlockItem(SableMaidRagdoll.TNT_CAKE_BLOCK.get());

        getBuilder("copy_ragdoll_id")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", "minecraft:item/stick");

        getBuilder("player_cheat_death")
                .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(0, 22.5F, 0).translation(0, 2.5F, 0).scale(1, 1, 1).end()
                .transform(ItemDisplayContext.GROUND)
                .rotation(0, 0, 0).translation(0, 2.5F, 0).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.HEAD)
                .translation(0, 14.5F, 0).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(45, 180, 0).translation(0, 0, -0.5F).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 0, 30).translation(-1, 5, 1.75F).scale(0.5F, 0.5F, 0.5F).end()
                .end();
    }
}
