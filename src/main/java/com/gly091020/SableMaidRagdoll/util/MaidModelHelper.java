package com.gly091020.SableMaidRagdoll.util;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.regex.Pattern;

public class MaidModelHelper {
    public static List<String> IGNORE_PART = List.of(
            "ahoge",
            "begShow",
            "blink2",
            "hurtBlink",
            "danmakuAttackShow"
    );
    public static List<String> SHOW_PART = List.of(
            "blink"
    );

    public static void resetModel(BedrockModel<?> model){
        model.getModelMap().values().forEach(MaidModelHelper::resetModel);

        IGNORE_PART.forEach(p -> hidePart(model.getModelMap().get(p)));
        SHOW_PART.forEach(p -> showPart(model.getModelMap().get(p)));
    }

    public static void hidePart(BedrockPart part){
        if(part == null)return;
        part.visible = false;
    }

    public static void showPart(BedrockPart part){
        if(part == null)return;
        part.visible = true;
    }

    public static void resetModel(BedrockPart part){
        // 943写的怎么是全局共享模型的?
        // 943的代码真让人着迷
        part.xRot = part.initRotX;
        part.yRot = part.initRotY;
        part.zRot = part.initRotZ;
    }

    public static MutableComponent paste943String(String s){
        final Pattern pattern = Pattern.compile("^\\{(.*)}$");
        var matcher = pattern.matcher(s);
        if(matcher.find())
            return Component.translatable(matcher.group(1));
        return Component.literal(s);
    }
}
