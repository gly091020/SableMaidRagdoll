package com.gly091020.SableMaidRagdoll;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = SableMaidRagdoll.MODID)
public class SableMaidRagdollConfig implements ConfigData {
    public boolean ragdollOnDeath = true;
    public boolean ragdollOnOwnerAttack = true;
    public boolean ragdollOnBox = true;
    public boolean cheatDeathBauble = true;
}
