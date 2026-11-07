package com.gly091020.SableMaidRagdoll.block.giant_slime;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum GiantSlimePart implements StringRepresentable {
    CORE,
    PART;

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
