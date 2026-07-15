package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.AbstractBedrockEntityModel;
import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = AbstractBedrockEntityModel.class, remap = false)
public interface BedrockModelAccessor {
    @Accessor("shouldRender")
    List<BedrockPart> getShouldRender();
}
