package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableMaidRagdoll.compat.CompatMods;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MaidRagdollMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "com.gly091020.SableMaidRagdoll.mixin.";
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(isModMixin(mixinClassName, "tlm"))return CompatMods.WINFOX_LITTLE_MAID.isLoaded();
        if(isModMixin(mixinClassName, "maid_spell"))return CompatMods.WINFOX_LITTLE_MAID_SPELL.isLoaded();
        if(isModMixin(mixinClassName, "love_loathe"))return CompatMods.LOVE_LOATHE.isLoaded();
        if(isModMixin(mixinClassName, "lmrb"))return CompatMods.LITTLE_MAID_REBIRTH.isLoaded();

        return true;
    }

    public boolean isModMixin(String mixinClassName, String dirName){
        return mixinClassName.contains(PACKAGE + dirName);
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
