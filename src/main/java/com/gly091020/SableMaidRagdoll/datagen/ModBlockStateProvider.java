package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, SableMaidRagdoll.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(InitBlocks.MAID_PART_BLOCK.get(),
                models().getBuilder("maid_part").texture("particle", modLoc("block/maid_part")));
        simpleBlock(InitBlocks.MAID_FAIRY_PART_BLOCK.get(),
                models().getBuilder("maid_fairy_part").texture("particle", modLoc("block/maid_part")));
        horizontalBlock(InitBlocks.MAID_DOLL_BLOCK.get(),
                models().getBuilder("maid_doll").texture("particle", mcLoc("block/white_wool")));
        tntCakeBlock();
    }

    private void tntCakeBlock() {
        var builder = getVariantBuilder(InitBlocks.TNT_CAKE_BLOCK.get());
        builder.partialState().with(BlockStateProperties.BITES, 0)
                .modelForState().modelFile(tntCakeModel("tnt_cake", "minecraft:block/cake")).addModel();
        for (int bites = 1; bites <= 6; bites++) {
            builder.partialState().with(BlockStateProperties.BITES, bites)
                    .modelForState()
                    .modelFile(tntCakeModel("tnt_cake_slice" + bites, "minecraft:block/cake_slice" + bites))
                    .addModel();
        }
    }

    private ModelFile tntCakeModel(String name, String parent) {
        return models().withExistingParent(name, mcLoc(parent))
                .texture("particle", mcLoc("block/cake_side"))
                .texture("bottom", mcLoc("block/cake_bottom"))
                .texture("top", mcLoc("block/cake_top"))
                .texture("side", mcLoc("block/cake_side"))
                .texture("inside", modLoc("block/tnt_cake_inner"));
    }
}
