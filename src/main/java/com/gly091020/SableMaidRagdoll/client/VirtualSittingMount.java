package com.gly091020.SableMaidRagdoll.client;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class VirtualSittingMount implements AutoCloseable {
    private final Entity entity;
    private final Display.ItemDisplay mount;

    private VirtualSittingMount(Entity entity, Display.ItemDisplay mount) {
        this.entity = entity;
        this.mount = mount;
    }

    public static VirtualSittingMount attach(Entity entity) {
        if (entity.getVehicle() != null || entity.level().isClientSide == false) {
            return new VirtualSittingMount(entity, null);
        }
        SittingDisplay mount = new SittingDisplay(entity.level());
        mount.setPos(entity.getX(), entity.getY(), entity.getZ());
        if (!entity.startRiding(mount, true)) {
            return new VirtualSittingMount(entity, null);
        }
        return new VirtualSittingMount(entity, mount);
    }

    @Override
    public void close() {
        if (mount != null && entity.getVehicle() == mount) {
            entity.stopRiding();
        }
    }

    private static final class SittingDisplay extends Display.ItemDisplay {
        private SittingDisplay(Level level) {
            super(EntityType.ITEM_DISPLAY, level);
        }

        @Override
        public boolean shouldRiderSit() {
            return true;
        }

        @Override
        protected boolean canAddPassenger(Entity passenger) {
            return getPassengers().isEmpty();
        }

        @Override
        public boolean isInvisible() {
            return true;
        }
    }
}
