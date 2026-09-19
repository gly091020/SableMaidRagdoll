package com.gly091020.SableMaidRagdoll.maid.lmrb.init;

import net.minecraft.resources.ResourceLocation;
import net.sistr.littlemaidrebirth.LMRBMod;
import com.gly091020.SableRagdollLib.api.RagdollTypeRegistry;

public class LMRBInitRagdollTypes {
    public static final ResourceLocation LITTLE_MAID_RAGDOLL_TYPE = ResourceLocation.fromNamespaceAndPath(LMRBMod.MODID, "maid");

    public static void init() {
        RagdollTypeRegistry.registry(LITTLE_MAID_RAGDOLL_TYPE,
                LMRBInitBlocks.LITTLE_MAID_PART_BLOCK::get,
                LMRBInitBlockEntities.LITTLE_MAID_PART_BLOCK_ENTITY::get);
    }
}
