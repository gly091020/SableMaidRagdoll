package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import com.gly091020.SableMaidRagdoll.init.InitSounds;
import com.gly091020.SableMaidRagdoll.network.ServerboundBroomManPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class BroomMixin {
    @Shadow
    public boolean horizontalCollision;

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZLnet/minecraft/world/phys/Vec3;)V"))
    public void man(MoverType moverType, Vec3 vec3, CallbackInfo ci){
        if(!SableMaidRagdoll.CONFIG.ragdollOnBroom)return;
        if(!((Object)this instanceof EntityBroom entityBroom))return;
        if(!horizontalCollision)return;
        Vec3 motion = getDeltaMovement();
        if(motion.lengthSqr() <= 0.5)return;
        double motionYaw = Math.toDegrees(Math.atan2(-motion.x, motion.z));
        double broomYaw = entityBroom.getYRot();
        double angle = Math.abs(((motionYaw - broomYaw + 180) % 360 + 360) % 360 - 180);
        double backAngle = Math.abs(((motionYaw - (broomYaw + 180) + 180) % 360 + 360) % 360 - 180);
        if(angle < 30 || backAngle < 30){
            PacketDistributor.sendToServer(new ServerboundBroomManPacket(entityBroom.getId(), motion));
            if(entityBroom.level().isClientSide)
                sableMaidRagdoll$playSound();
        }
    }

    @Unique
    @OnlyIn(Dist.CLIENT)
    private static void sableMaidRagdoll$playSound(){
        if(SableMaidRagdoll.CONFIG.sounds.broomMan)
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(InitSounds.BROOM_MAN, 1));
    }
}
