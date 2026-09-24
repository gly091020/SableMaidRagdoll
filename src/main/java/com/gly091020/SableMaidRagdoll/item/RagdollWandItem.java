package com.gly091020.SableMaidRagdoll.item;

import com.gly091020.SableMaidRagdoll.block.maid_doll.MaidDollData;
import com.gly091020.SableMaidRagdoll.init.InitDataComponents;
import com.gly091020.SableMaidRagdoll.maid.api.MaidRagdollTypesManager;
import com.gly091020.SableRagdollLib.api.ScheduleManager;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 布娃娃魔杖：绑定模型的方式与 {@link PlayerCheatDeathItem} 相同，长按使用把准星指向的生物变成布娃娃。
 */
public class RagdollWandItem extends Item {
    /** 蓄力所需 tick 数。 */
    private static final int CHARGE_TICKS = 40;
    /** 每级效率减少的蓄力 tick 数。 */
    private static final int CHARGE_TICKS_PER_EFFICIENCY = 10;
    /** 效率达到该等级后不再蓄力，直接施法。 */
    private static final int INSTANT_CAST_EFFICIENCY = 5;
    /** 瞄准射线长度，与正常交互距离无关。 */
    private static final double RANGE = 64.0D;
    /** 命中判定放大值，方便瞄准小型生物。 */
    private static final double HITBOX_INFLATE = 0.3D;
    /** 使用后的冷却。 */
    private static final int COOLDOWN = 20;
    /** 每级效率减少的冷却百分比。 */
    private static final float COOLDOWN_REDUCTION_PER_EFFICIENCY = 0.2F;
    /** 风爆把布娃娃击飞的力度（格/tick）。 */
    private static final double WIND_BURST_POWER = 6.0D;
    /** 风爆每级额外增加的力度。 */
    private static final double WIND_BURST_POWER_PER_LEVEL = 2.0D;
    /** 蓄力时每两 tick 在目标周围生成的粒子数。 */
    private static final int CHARGE_PARTICLES = 2;
    /** 施法完成瞬间生成的粒子数。 */
    private static final int CAST_PARTICLES = 100;
    /** 粒子球半径。 */
    private static final double PARTICLE_RADIUS = 3D;
    /** 粒子飞向目标的速度（格/tick）。 */
    private static final double PARTICLE_SPEED = 0.5D;
    /**
     * 蓄力时信标环境音的重播间隔。
     * 原版信标是 80 tick 一次，而 beacon/ambient.ogg 本身有 5.8 秒，2 秒的蓄力期间只播一次会让声音一直处在渐强阶段，
     * 所以这里缩短到 20 tick（1 秒），保证整个施法过程都有声音在响。
     */
    private static final int CHARGE_SOUND_INTERVAL = 20;

    public RagdollWandItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof TamableAnimal maid) || maid.getOwner() == null || !maid.getOwner().is(player))
            return super.interactLivingEntity(stack, player, target, hand);
        var data = MaidRagdollTypesManager.getDataFromEntity(maid);
        if (data == null) return InteractionResult.PASS;
        stack.set(InitDataComponents.MAID_DOLL_DATA, data);
        if (player.level().isClientSide)
            player.sendSystemMessage(Component.translatable("item.sablemaidragdoll.ragdoll_wand.connect", data.modelID()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                stack.remove(InitDataComponents.MAID_DOLL_DATA.get());
                player.sendSystemMessage(Component.translatable("item.sablemaidragdoll.ragdoll_wand.clear"));
            }
            return InteractionResultHolder.success(stack);
        }
        // 服务端也挡一次，免得客户端没收到冷却包时还能用
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }
        var data = stack.get(InitDataComponents.MAID_DOLL_DATA.get());
        if (data == null) {
            return InteractionResultHolder.fail(stack);
        }
        // 准星上没有有效目标就直接失败，不进入蓄力
        var target = findTarget(player);
        if (target == null) {
            return InteractionResultHolder.fail(stack);
        }
        // 效率 V：不蓄力，直接施法
        if (getChargeTicks(stack, level) <= 0) {
            if (level.isClientSide) {
                spawnConvergingParticles(level, target, CAST_PARTICLES, PARTICLE_SPEED * 5D);
            } else if (player instanceof ServerPlayer serverPlayer && cast(serverPlayer, target, stack, data)) {
                addCooldown(stack, serverPlayer, getCooldownTicks(stack, level));
            }
            return InteractionResultHolder.success(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return Math.max(1, getChargeTicks(stack, entity.level()));
    }

    /**
     * 实际蓄力时间：每级效率减少 {@link #CHARGE_TICKS_PER_EFFICIENCY} tick，
     * 达到 {@link #INSTANT_CAST_EFFICIENCY} 级时返回 0 表示直接施法。
     */
    public static int getChargeTicks(ItemStack stack, Level level) {
        int efficiency = getEfficiencyLevel(stack, level);
        if (efficiency >= INSTANT_CAST_EFFICIENCY) return 0;
        return Math.max(1, CHARGE_TICKS - CHARGE_TICKS_PER_EFFICIENCY * efficiency);
    }

    /** 实际冷却：按效率每级减少 {@link #COOLDOWN_REDUCTION_PER_EFFICIENCY} 的比例。 */
    public static int getCooldownTicks(ItemStack stack, Level level) {
        int efficiency = getEfficiencyLevel(stack, level);
        float multiplier = Math.max(0.1F, 1.0F - COOLDOWN_REDUCTION_PER_EFFICIENCY * efficiency);
        return Math.max(0, Math.round(COOLDOWN * multiplier));
    }

    private static int getEfficiencyLevel(ItemStack stack, Level level) {
        return getEnchantLevel(stack, level, Enchantments.EFFICIENCY);
    }

    private static int getEnchantLevel(ItemStack stack, Level level, ResourceKey<Enchantment> enchantment) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(enchantment)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (!(living instanceof Player player)) return;
        var target = findTarget(player);
        // 目标失效（死亡、被挡住、看丢了）就直接终止施法
        if (target == null) {
            player.stopUsingItem();
            return;
        }
        int elapsed = Math.max(1, getChargeTicks(stack, level)) - remaining;
        if (!level.isClientSide) {
            // 蓄力期间播放信标保持激活的环境音
            if (elapsed % CHARGE_SOUND_INTERVAL == 0)
                level.playSound(null, target.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        // 最后一 tick 说明蓄力即将完成，来一发大量粒子
        if (remaining <= 1) {
            spawnConvergingParticles(level, target, CAST_PARTICLES, PARTICLE_SPEED * 5D);
            return;
        }
        if (elapsed % 2 != 0) return;
        spawnConvergingParticles(level, target, CHARGE_PARTICLES, PARTICLE_SPEED);
    }

    /** 在目标周围半径 r 的球面上生成粒子，速度全部指向目标中心。 */
    private static void spawnConvergingParticles(Level level, LivingEntity target, int count, double speed) {
        var random = level.random;
        var center = target.position().add(0, target.getBbHeight() * 0.5D, 0);
        for (int i = 0; i < count; i++) {
            // 球面上的均匀随机点
            double cosPhi = random.nextDouble() * 2.0D - 1.0D;
            double sinPhi = Math.sqrt(1.0D - cosPhi * cosPhi);
            double theta = random.nextDouble() * Math.PI * 2.0D;
            double dx = sinPhi * Math.cos(theta);
            double dy = cosPhi;
            double dz = sinPhi * Math.sin(theta);
            level.addParticle(ParticleTypes.CLOUD,
                    center.x + dx * PARTICLE_RADIUS, center.y + dy * PARTICLE_RADIUS, center.z + dz * PARTICLE_RADIUS,
                    -dx * speed, -dy * speed, -dz * speed);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (level.isClientSide || !(living instanceof ServerPlayer player)) return stack;
        var data = stack.get(InitDataComponents.MAID_DOLL_DATA.get());
        if (data == null) return stack;

        var target = findTarget(player);
        if (target == null) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.5F);
            return stack;
        }
        if (!cast(player, target, stack, data)) {
            return stack;
        }
        addCooldown(stack, player, getCooldownTicks(stack, level));
        return stack;
    }

    private static void addCooldown(ItemStack stack, Player player, int ticks) {
        if(ticks <= 0)return;
        player.getCooldowns().addCooldown(stack.getItem(), ticks);
    }

    private static boolean cast(ServerPlayer player, LivingEntity target, ItemStack stack, MaidDollData data) {
        ServerLevel level = player.serverLevel();
        var motion = target.getDeltaMovement().scale(2);
        var ragdoll = MaidRagdollTypesManager.createRagdollFromDoll(level, target,
                target.position().add(0, 0.3D, 0), new Vec3(0, -target.getYHeadRot(), 0), motion, Vec3.ZERO, false, data);
        if (ragdoll == null) return false;
        ragdoll.getExtraData().putString("PCDI_soundID", data.soundID());
        ragdoll.getExtraData().putString("PCDI_typeID", data.ragdollType());
        // 风爆附魔：在目标位置放一次风暴，并把布娃娃击飞
        int windBurst = getEnchantLevel(stack, level, Enchantments.WIND_BURST);
        if (windBurst > 0) spawnWindBurst(level, target.position().add(0, 0.3D, 0));
        // 与 PlayerCheatDeathItem 一致，等刚体创建完成后在把生物挂上去
        double windPower = WIND_BURST_POWER + WIND_BURST_POWER_PER_LEVEL * Math.max(0, windBurst - 1);
        ScheduleManager.scheduleDelayed(level, 2, () -> {
            ragdoll.addEntity(target);
            if (windBurst > 0) ragdoll.addLinearImpulse(new Vec3(0, windPower, 0), false);
        });
        level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 0.5F);
        return true;
    }

    /** 风爆的粒子与音效，参数和原版风爆附魔一致。 */
    private static void spawnWindBurst(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 沿玩家视线寻找可施法的生物，客户端也会用到（目标高亮）。 */
    public static @Nullable LivingEntity findTarget(Player player) {
        var level = player.level();
        var start = player.getEyePosition();
        var look = player.getLookAngle();
        var end = start.add(look.scale(RANGE));

        double maxDistance = RANGE;
        var blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS)
            maxDistance = start.distanceTo(blockHit.getLocation());

        LivingEntity best = null;
        double bestDistance = maxDistance;
        for (var candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(1), e -> isValidTarget(player, e))) {
            var hit = candidate.getBoundingBox().inflate(HITBOX_INFLATE).clip(start, end);
            if (hit.isEmpty()) continue;
            double distance = start.distanceTo(hit.get());
            if (distance >= bestDistance) continue;
            bestDistance = distance;
            best = candidate;
        }
        return best;
    }

    private static boolean isValidTarget(Player player, LivingEntity entity) {
        return entity != player && entity.isAlive() && !entity.isSpectator() && !(entity.getVehicle() instanceof PartSeat);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(isWineFox(stack) ? "item.sablemaidragdoll.ragdoll_wand.fox.desc" :
                "item.sablemaidragdoll.ragdoll_wand.desc").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.sablemaidragdoll.ragdoll_wand.tip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.sablemaidragdoll.ragdoll_wand.tip1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.sablemaidragdoll.ragdoll_wand.tip2").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        return isWineFox(stack) ?
                Component.translatable("item.sablemaidragdoll.ragdoll_wand.fox") :
                super.getName(stack);
    }

    private static boolean isWineFox(ItemStack stack){
        var data = stack.get(InitDataComponents.MAID_DOLL_DATA);
        if(data == null)return false;
        return data.ragdollType().equals("tlm") && data.modelID().contains("wine") && data.modelID().contains("fox");
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var data = stack.get(InitDataComponents.MAID_DOLL_DATA);
        if(data == null)return Optional.empty();
        return Optional.ofNullable(MaidRagdollTypesManager.getTooltipImage(data));
    }
}
