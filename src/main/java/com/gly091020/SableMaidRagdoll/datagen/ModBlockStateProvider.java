package com.gly091020.SableMaidRagdoll.datagen;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, SableMaidRagdoll.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(SableMaidRagdoll.MAID_PART_BLOCK.get(),
                models().getBuilder("maid_part").texture("particle", modLoc("block/maid_part")));
        simpleBlock(SableMaidRagdoll.MAID_FAIRY_PART_BLOCK.get(),
                models().getBuilder("maid_fairy_part").texture("particle", modLoc("block/maid_part")));
        horizontalBlock(SableMaidRagdoll.MAID_DOLL_BLOCK.get(),
                models().getBuilder("maid_doll").texture("particle", mcLoc("block/white_wool")));
    }
}
