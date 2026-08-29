package com.gly091020.SableMaidRagdoll.mixin.maid_spell;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.github.yimeng261.maidspell.event.MaidSpellAllyEvents")
public class MaidSpellAllyEventsMixin {
    @WrapMethod(method = "onLivingIncomingDamage")
    private static void attackWineFoxYes(LivingIncomingDamageEvent event, Operation<Void> original){
        // Do nothing~
    }
}
