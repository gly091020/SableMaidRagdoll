package com.gly091020.SableMaidRagdoll.block.tnt_cake;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TNTCakeBlock extends CakeBlock {
    public TNTCakeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack p_316238_, BlockState p_316837_, Level p_316766_, BlockPos p_316227_, Player p_316853_, InteractionHand p_316422_, BlockHitResult p_316869_) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected InteractionResult useWithoutItem(BlockState p_316481_, Level p_316406_, BlockPos p_316218_, Player p_316212_, BlockHitResult p_316525_) {
        if (p_316406_.isClientSide) {
            if (eat(p_316406_, p_316218_, p_316481_, p_316212_).consumesAction()) {
                return InteractionResult.SUCCESS;
            }

            if (p_316212_.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }

        var eat = eat(p_316406_, p_316218_, p_316481_, p_316212_);
        if(eat == InteractionResult.SUCCESS){
            p_316212_.addEffect(new MobEffectInstance(MobEffects.POISON, 5 * 20, 1, false, true));
        }
        return eat;
    }
}
