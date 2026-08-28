package com.gly091020.SableMaidRagdoll.item.spawn_egg;

import com.github.tartaricacid.touhoulittlemaid.entity.info.models.ServerMaidModels;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class WineFoxSpawnEgg extends SMRDeferredSpawnEggItem {
    private static final String DEFAULT_MODEL_ID = "touhou_little_maid:hakurei_reimu";
    public WineFoxSpawnEgg() {
        super(() -> InitEntities.MAID.get(), 0xe54444, 0xf4b33f);
    }

    @Override
    public void afterSpawn(Entity owner, Entity entity) {
        if(entity.level().isClientSide)return;
        if(entity instanceof EntityMaid maid){
            maid.setModelId(getRandomMaid(maid.getRandom()));
            if(owner instanceof Player player && player.isShiftKeyDown())
                maid.setOwnerUUID(player.getUUID());
        }
    }

    public static String getRandomMaid(RandomSource randomSource){
        var all = ServerMaidModels.getInstance().getModelIdSet().stream().filter(modelID ->
                modelID.contains("wine") && modelID.contains("fox")).toList();
        if(all.isEmpty())
            return DEFAULT_MODEL_ID;
        return all.get(randomSource.nextInt(0, all.size()));
    }
}
