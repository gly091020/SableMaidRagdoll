package com.gly091020.SableMaidRagdoll.mixin.lmrb;

import com.gly091020.SableMaidRagdoll.init.InitItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.sistr.littlemaidrebirth.entity.LittleMaidEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.sistr.littlemaidrebirth.entity.LMInteractionHandler")
public class LMInteractionHandlerMixin {
    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void onCheatDeathItemUse(LittleMaidEntity maid, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        ItemStack stack = player.getItemInHand(hand);
        if(stack.is(InitItems.PLAYER_CHEAT_DEATH_ITEM) || stack.is(InitItems.RAGDOLL_WAND_ITEM)) {
            stack.interactLivingEntity(player, maid, hand);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
