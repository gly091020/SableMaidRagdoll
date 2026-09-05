package com.gly091020.SableMaidRagdoll.mixin;

import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.gly091020.SableMaidRagdoll.client.renderer.block.MaidPartRenderCache;
import com.gly091020.SableMaidRagdoll.geo.GeoMaidModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TLM 重载模型包（/tlm pack reload、F3+T、配置触发的下载）时会清空 MAID_MODELS 并重建模型实例，
 * 需要同步清空布娃娃渲染缓存，避免继续引用旧模型。
 */
@Mixin(CustomPackLoader.class)
public class CustomPackLoaderMixin {
    @Inject(method = "reloadPacks", at = @At("TAIL"))
    private static void sableMaidRagdoll$clearRenderCache(CallbackInfo ci) {
        MaidPartRenderCache.clear();
        GeoMaidModelRenderer.clear();
    }
}
