package com.gly091020.SableMaidRagdoll;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = SableMaidRagdoll.MODID)
public class SableMaidRagdollConfig implements ConfigData {
    public boolean ragdollOnDeath = true;
    public boolean ragdollOnOwnerAttack = true;
    public boolean ragdollOnBox = true;
    public boolean ragdollOnSpecialDamage = false;
    public boolean cheatDeathBauble = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public Sounds sounds = new Sounds();

    // todo:未完成
    @ConfigEntry.Gui.Excluded
    public boolean playerCheatDeathItem = false;

    public static class Sounds{
        public boolean metalPipe = false;
        public boolean hungry = false;
        public boolean drop = false;
    }
}
