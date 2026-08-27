package com.gly091020.SableMaidRagdoll.item.spawn_egg;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableRagdollLib.common.DefFileLoader;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class RagdollableMaidSpawnEgg extends SMRDeferredSpawnEggItem {
    private static final String DEFAULT_MODEL_ID = "touhou_little_maid:hakurei_reimu";
    public RagdollableMaidSpawnEgg() {
        super(() -> InitEntities.MAID.get(), 0x2280ba, 0xf4b33f);
    }

    @Override
    public void afterSpawn(Entity entity) {
        if(entity.level().isClientSide)return;
        if(entity instanceof EntityMaid maid)maid.setModelId(getRandomMaid(maid.getRandom()));
    }

    public static String getRandomMaid(RandomSource randomSource){
        var all = DefFileLoader.getAllKeys().stream().filter(r -> r.getNamespace().equals(SableMaidRagdoll.MODID)).toList();
        if(all.isEmpty())
            return DEFAULT_MODEL_ID;
        var id = all.get(randomSource.nextInt(0, all.size()));
        return id.getPath().replace("/", ":");
    }
}
