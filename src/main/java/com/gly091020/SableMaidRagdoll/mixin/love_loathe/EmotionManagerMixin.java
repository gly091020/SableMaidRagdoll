package com.gly091020.SableMaidRagdoll.mixin.love_loathe;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(targets = {
        "com.github.JumDa5he.callresponse.compat.emotion.EmotionBetrayalManager",
        "com.github.JumDa5he.callresponse.compat.emotion.EmotionDevotedManager",
        "com.github.JumDa5he.callresponse.compat.emotion.EmotionDotingManager",
        "com.github.JumDa5he.callresponse.compat.emotion.EmotionForgettingManager",
        "com.github.JumDa5he.callresponse.compat.emotion.EmotionPassiveManager",
}, remap = false)
@Pseudo
public class EmotionManagerMixin {
    @Redirect(method = "onServerTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    public List<EntityMaid> breakNoAI(Level instance, Class<EntityMaid> aClass, AABB aabb){
        return instance.getEntitiesOfClass(aClass, aabb, m -> !m.isNoAi());
    }
}
