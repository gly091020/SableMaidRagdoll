package com.gly091020.SableMaidRagdoll.geo;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HardCodeModelHandler {
    private static final Map<ResourceLocation, Consumer<AnimatedGeoModel>> FUNCTIONS = new HashMap<>();

    public static void called(ResourceLocation modelID, AnimatedGeoModel model){
        var f = FUNCTIONS.get(modelID);
        if(f != null)f.accept(model);
    }

    public static void init(){
        FUNCTIONS.clear();
        FUNCTIONS.put(ResourceLocation.fromNamespaceAndPath("geckolib", "winefox"), animatedGeoModel -> {
            var bones = animatedGeoModel.bones();
            var a = bones.get("EyeBrow");
            if(a != null)
                a.setPosition(0, -0.75f, 0);
            a = bones.get("Eyelid");
            if(a != null)
                a.setPosition(0, 0, 1);
            a = bones.get("weixiao");
            if(a != null) {
                a.setHidden(false);
                a.setPosition(0, 0.25f, -1);
            }
        });
    }

    static {
        init();
    }
}
