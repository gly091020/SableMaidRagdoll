package com.gly091020.SableMaidRagdoll.mixin.maid_spell;

import com.github.yimeng261.maidspell.compat.irons_spellbooks.entity.winefox.MagicalWinefoxBossEntity;
import com.github.yimeng261.maidspell.compat.irons_spellbooks.entity.winefox.WinefoxDialogue;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.compat.maid_spell.MaidSpellUtil;
import com.gly091020.SableMaidRagdoll.init.InitTags;
import com.gly091020.SableMaidRagdoll.init.InitTrigger;
import com.gly091020.SableMaidRagdoll.util.MaidRagdollAdvancementEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(MagicalWinefoxBossEntity.class)
public abstract class MagicalWinefoxBossEntityMixin {
    @Shadow
    public abstract boolean isSeated();

    @Shadow
    private @Nullable UUID challengerId;

    @Inject(method = "hurt", at = @At(value = "HEAD"))
    public void saySomething(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        // 想拿着铲子就打败魔女吗？
        if(isSeated() && SableMaidRagdoll.CONFIG.ragdollOnOwnerAttack &&
                source.getEntity() instanceof Player player &&
                source.getWeaponItem() != null &&
                source.getWeaponItem().is(InitTags.MAID_TO_RAGDOLL_TAG)){
            WinefoxDialogue.sendToPlayer(player, MaidSpellUtil.getRandomItemSpeak());
        }
    }

    @Inject(method = "acceptChallenge", at = @At("HEAD"))
    public void clearHurtAmount(Player challenger, ItemStack invitation, CallbackInfo ci){
        var self = (Entity)(Object)this;
        if(self.getPersistentData().contains("RagdollDamage"))
            self.getPersistentData().remove("RagdollDamage");
    }

    @WrapOperation(method = "beginDefeat", at = @At(value = "INVOKE", target = "Lcom/github/yimeng261/maidspell/compat/irons_spellbooks/entity/winefox/WinefoxDialogue;victory(ZZ)Ljava/util/List;"))
    public List<Component> warpVictoryMessage(boolean restricted, boolean trueDamage, Operation<List<Component>> original){
        var self = (Entity)(Object)this;
        if(self.getPersistentData().contains("RagdollDamage") && self.getPersistentData().getFloat("RagdollDamage") > 600 * 0.5) {
            self.getPersistentData().remove("RagdollDamage");
            if(challengerId != null && self.level() instanceof ServerLevel serverLevel && serverLevel.getEntity(challengerId) instanceof ServerPlayer player) {
                InitTrigger.EVENT_TRIGGER.get().trigger(player, MaidRagdollAdvancementEvents.CRUSH_WINE_FOX_BOSS.getName());
            }
            return List.of(MaidSpellUtil.getVictorySpeak());
        }
        return original.call(restricted, trueDamage);
    }
}
