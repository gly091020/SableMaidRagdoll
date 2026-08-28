package com.gly091020.SableMaidRagdoll;

import com.gly091020.SableRagdollLib.client.button.Button;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = SableMaidRagdoll.MODID)
public class SableMaidRagdollConfig implements ConfigData {
    public boolean ragdollOnDeath = true;
    public boolean ragdollOnOwnerAttack = true;
    public boolean ragdollOnBox = true;
    public boolean ragdollOnSpecialDamage = false;
    public boolean ragdollOnBroom = false;
    public boolean maidKnockback = true;
    public boolean maidEat = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public Items items = new Items();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public LoveAndLoathe loveAndLoathe = new LoveAndLoathe();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public PlayerRagdoll playerRagdoll = new PlayerRagdoll();

    @ConfigEntry.Gui.CollapsibleObject()
    public Sounds sounds = new Sounds();

    public static class Items{
        @ConfigEntry.Gui.RequiresRestart
        public boolean enableDollTab = false;
        public boolean cheatDeathBauble = true;
        public boolean playerCheatDeathItem = true;
        public boolean maidMace = true;
        public boolean tntCake = true;
        public boolean sonicWave = true;
        public boolean spawnEggs = true;
    }

    public static class Sounds{
        public boolean metalPipe = false;
        public boolean hungry = false;
        public boolean drop = false;
        public boolean bigDog = false;
        public boolean GCJCry = false;
        public boolean watermelonHurt = false;
        public boolean broomMan = false;
    }

    public static class LoveAndLoathe{
        @ConfigEntry.Gui.RequiresRestart
        public boolean moreAIFunction = true;
        public boolean drop = true;
    }

    public static class PlayerRagdoll{
        public boolean attackToRagDoll = true;
    }

    @Button("open_lib_config")
    public Void openLibConfig = null;
}
