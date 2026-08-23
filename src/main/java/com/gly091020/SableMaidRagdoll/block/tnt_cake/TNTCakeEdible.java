package com.gly091020.SableMaidRagdoll.block.tnt_cake;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.edible.CakeEdible;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TNTCakeEdible extends CakeEdible {
    @Override
    public boolean shouldMoveTo(EntityMaid maid, BlockPos pos, BlockState state) {
        return state.is(SableMaidRagdoll.TNT_CAKE_BLOCK);
    }

    @Override
    public int getFavorabilityPoints(EntityMaid maid, BlockPos pos, BlockState state) {
        return 0;
    }

    @Override
    public boolean consume(EntityMaid maid, BlockPos pos, BlockState state) {
        var r = super.consume(maid, pos, state);
        if(r)
            maid.level().explode(null, null, TNTExplosionDamageCalculator.INSTANCE, maid.position(), 3, false, Level.ExplosionInteraction.BLOCK);
        return r;
    }

    public static class TNTExplosionDamageCalculator extends ExplosionDamageCalculator{
        public static final TNTExplosionDamageCalculator INSTANCE = new TNTExplosionDamageCalculator();
        private TNTExplosionDamageCalculator(){}
        @Override
        public boolean shouldBlockExplode(Explosion p_46094_, BlockGetter p_46095_, BlockPos p_46096_, BlockState p_46097_, float p_46098_) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return entity instanceof EntityMaid ? 3 : 0;
        }

        @Override
        public float getEntityDamageAmount(Explosion p_311793_, Entity entity) {
            return entity instanceof EntityMaid ? 1 : 0;
        }
    }
}
