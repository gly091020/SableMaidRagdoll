package com.gly091020.SableMaidRagdoll.mixin.model;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = BedrockPart.class, remap = false)
public interface BedrockPartMixin {
    @Invoker("compile")
    void invokeCompile(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int lightmap,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha
    );
}
