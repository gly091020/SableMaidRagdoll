package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

public class InitTags {
    public static final TagKey<Item> MAID_TO_RAGDOLL_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "maid_to_ragdoll"));
    public static final TagKey<DamageType> ALWAYS_TO_RAGDOLL_TAG = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "always_to_ragdoll"));
    public static final TagKey<DamageType> LAOWU_HURT_DANCE = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "laowu_hurt_dance"));
}
