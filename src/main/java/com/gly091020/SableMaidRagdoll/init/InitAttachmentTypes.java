package com.gly091020.SableMaidRagdoll.init;

import com.gly091020.SableMaidRagdoll.SableMaidRagdoll;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class InitAttachmentTypes {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SableMaidRagdoll.MODID);
    public static final ResourceLocation EMPTY_EMOJI = ResourceLocation.fromNamespaceAndPath(SableMaidRagdoll.MODID, "empty");
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ResourceLocation>> EMOJI_ATTACHMENT = ATTACHMENT_TYPES.register("emoji", () ->
            AttachmentType.builder(() -> EMPTY_EMOJI).serialize(ResourceLocation.CODEC).sync(ResourceLocation.STREAM_CODEC).build());

    public static void init(IEventBus bus){
        ATTACHMENT_TYPES.register(bus);
    }
}
