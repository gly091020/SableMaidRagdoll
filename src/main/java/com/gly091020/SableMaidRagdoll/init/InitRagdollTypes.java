package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollPartRecognizer;
import com.gly091020.SableRagdollLib.api.RagdollTypeRegistry;
import com.gly091020.SableRagdollLib.api.control.RagdollPartRecognizerRegistry;
import net.minecraft.resources.ResourceLocation;

public class InitRagdollTypes {
    public static final ResourceLocation RAGDOLL_TYPE = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "maid");
    public static final ResourceLocation FAIRY_RAGDOLL_TYPE = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "maid_fairy");

    public static void init(){
        RagdollTypeRegistry.registry(InitRagdollTypes.RAGDOLL_TYPE, InitBlocks.MAID_PART_BLOCK::get, InitBlockEntities.MAID_PART_BLOCK_ENTITY::get);
        RagdollTypeRegistry.registry(InitRagdollTypes.FAIRY_RAGDOLL_TYPE, InitBlocks.MAID_FAIRY_PART_BLOCK::get, InitBlockEntities.MAID_FAIRY_PART_BLOCK_ENTITY::get);
        RagdollPartRecognizerRegistry.register(new MaidRagdollPartRecognizer());
    }
}
