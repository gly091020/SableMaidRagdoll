package com.gly091020.SableMaidRagdoll.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code Entity#setSharedFlag} 是 protected，这里开个口子，
 * 让客户端能给实体打上原版发光标记（客户端读的是共享标记，{@code setGlowingTag} 在客户端是无效的）。
 */
@Mixin(Entity.class)
public interface EntitySharedFlagAccessor {
    @Invoker("setSharedFlag")
    void smr$setSharedFlag(int flag, boolean value);
}
