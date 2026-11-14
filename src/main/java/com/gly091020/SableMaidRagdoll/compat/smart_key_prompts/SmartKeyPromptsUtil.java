package com.gly091020.SableMaidRagdoll.compat.smart_key_prompts;

import com.gly091020.SableMaidRagdoll.client.SableMaidRagdollClient;
import com.gly091020.SableRagdollLib.SableRagdollLibClient;
import com.gly091020.SableRagdollLib.client.RagdollControlClient;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import com.mafuyu404.smartkeyprompts.init.KeyPrompt;
import com.mafuyu404.smartkeyprompts.util.KeyUtils;
import com.mafuyu404.smartkeyprompts.util.PromptUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class SmartKeyPromptsUtil {
    public static void init(){
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    private static class EventHandler{
        @SubscribeEvent
        public static void onRenderHud(ClientTickEvent.Pre event){
            var player = Minecraft.getInstance().player;
            if(player == null)return;
            if(!(player.getVehicle() instanceof PartSeat))return;
            showRagdollKey();
            if(RagdollControlClient.isControlling())
                showControlKey();
        }

        private static void showControlKey(){
            showKeyMapping(SableRagdollLibClient.GRAB);
            showKeyMapping(SableMaidRagdollClient.MAKE_SOUND);
            showKeyMapping(SableMaidRagdollClient.OPEN_EMOJI);
        }

        private static void showRagdollKey(){
            showKeyMapping(SableRagdollLibClient.SWITCH_CONTROL, RagdollControlClient.isControlling() ?
                    "text.sablemaidragdoll.switch_control.sleep" : "text.sablemaidragdoll.switch_control.stand_up");
            showKeyMapping(Minecraft.getInstance().options.keyTogglePerspective);
        }

        private static void showKeyMapping(KeyMapping mapping){
            PromptUtils.show(mapping.getCategory(), mapping.getName());
        }

        private static void showKeyMapping(KeyMapping mapping, String desc){
            new KeyPrompt("", KeyUtils.getKeyByDesc(mapping.getName()), desc, false).toGroup(mapping.getCategory());
        }
    }
}
