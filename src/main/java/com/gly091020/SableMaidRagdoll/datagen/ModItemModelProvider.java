package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import com.gly091020.SableMaidRagdoll.init.InitItems;
import com.gly091020.SableMaidRagdoll.maid.tlm.init.TLMInitItems;
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
        basicItem(InitItems.CHEAT_DEATH_BAUBLE_ITEM.get());
        basicItem(InitItems.MAID_MACE_ITEM.get());
        basicItem(InitItems.MOD_ICON_ITEM.get());
        basicItem(InitItems.SONIC_WAVE_ITEM.get());
        spawnEggItem(TLMInitItems.RAGDOLLABLE_MAID_SPAWN_EGG.get());
        spawnEggItem(TLMInitItems.WINE_FOX_SPAWN_EGG.get());
        spawnEggItem(TLMInitItems.RAGDOLLABLE_WINE_FOX_SPAWN_EGG.get());
        simpleBlockItem(InitBlocks.TNT_CAKE_BLOCK.get());

        getBuilder("mob_cannon")
                .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F, 0.375F, 0.375F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F, 0.375F, 0.375F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 45, 0).translation(0, 0, 0).scale(0.4F, 0.4F, 0.4F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, -135, 0).translation(0, 0, 0).scale(0.4F, 0.4F, 0.4F).end()
                .transform(ItemDisplayContext.GROUND)
                .rotation(0, 0, 0).translation(0, 3, 0).scale(0.25F, 0.25F, 0.25F).end()
                .transform(ItemDisplayContext.GUI)
                .rotation(30, -135, 0).translation(0, -3, 0).scale(0.3F, 0.3F, 0.3F).end()
                .transform(ItemDisplayContext.FIXED)
                .rotation(0, 0, 0).translation(0, 0, 0).scale(0.5F, 0.5F, 0.5F).end()
                .end();

        getBuilder("copy_ragdoll_id")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", "minecraft:item/stick");

        var giantSlime = getBuilder("giant_slime_block")
                .texture("all", mcLoc("block/slime_block"))
                .texture("particle", mcLoc("block/slime_block"));
        float cellSize = 8.0F;
        float gap = 0.2F;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    float minX = x * cellSize;
                    float minY = y * cellSize;
                    float minZ = z * cellSize;
                    giantSlime.element()
                            .from(minX + gap, minY + gap, minZ + gap)
                            .to(minX + cellSize - gap, minY + cellSize - gap, minZ + cellSize - gap)
                            .allFaces((direction, face) -> face.texture("#all"))
                            .end();
                }
            }
        }
        giantSlime.transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(30, 225, 0).scale(0.7F, 0.7F, 0.7F).end()
                .transform(ItemDisplayContext.GROUND)
                .scale(0.6F, 0.6F, 0.6F).end()
                .transform(ItemDisplayContext.FIXED)
                .scale(0.7F, 0.7F, 0.7F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .scale(0.5F, 0.5F, 0.5F).end()
                .end();

        getBuilder("ragdoll_wand")
                .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                .transforms()
                .transform(ItemDisplayContext.GUI)
                .rotation(0, 22.5F, 0).translation(0, 0, 0).scale(1, 1, 1).end()
                .transform(ItemDisplayContext.GROUND)
                .rotation(0, 0, 0).translation(0, 0, 0).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.HEAD)
                .translation(0, 14.5F, 0).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(45, 180, 0).translation(0, 0, -0.5F).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(45, -180, 0).translation(0, 0, -0.5F).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 0, 30).translation(-1, 5, 1.75F).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 0, -30).translation(1, 5, 1.75F).scale(0.5F, 0.5F, 0.5F).end()
                .end();

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
