package com.gly091020.SableMaidRagdoll.maid.lmrb.client.renderer;

import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.maidmodel.ModelMultiBase;
import net.sistr.littlemaidmodelloader.maidmodel.ModelRenderer;
import net.sistr.littlemaidmodelloader.multimodel.IMultiModel;
import net.sistr.littlemaidmodelloader.resource.manager.LMModelManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 按布娃娃定义文件缓存 LMML 模型实例与模型部件查找结果。
 * <p>
 * LMML 的 ModelRenderer 绝大多数只有位置没有名字（boxName 为 null），
 * 因此部件按模型类中的公开字段名（如 bipedHead / bipedLeftArm）反射获取。
 */
public final class LittleMaidPartRenderCache {
    private static final Map<String, Entry> CACHE = new HashMap<>();

    private LittleMaidPartRenderCache() {
    }

    public static void clear() {
        CACHE.clear();
    }

    public static Entry get(String modelName) {
        return CACHE.computeIfAbsent(modelName.toLowerCase(Locale.ROOT), LittleMaidPartRenderCache::load);
    }

    private static Entry load(String modelName) {
        // 定义文件名即模型名，例如 data/littlemaidrebirth/ragdoll/default.json -> littlemaidrebirth:default
        return new Entry(resolveModel(modelName));
    }

    @Nullable
    public static ModelMultiBase resolveModel(String modelName) {
        IMultiModel model = LMModelManager.INSTANCE.getModel(modelName, IHasMultiModel.Layer.SKIN).orElse(null);
        return model instanceof ModelMultiBase multiModel ? multiModel : null;
    }

    public static final class Entry {
        private final @Nullable ModelMultiBase model;
        private final Map<String, ModelRenderer> nodes = new HashMap<>();

        public Entry(@Nullable ModelMultiBase model) {
            this.model = model;
        }

        public @Nullable ModelMultiBase model() {
            return model;
        }

        public @Nullable ModelRenderer node(String fieldName) {
            if (model == null)
                return null;
            return nodes.computeIfAbsent(fieldName, this::find);
        }

        private @Nullable ModelRenderer find(String fieldName) {
            try {
                var field = model.getClass().getField(fieldName);
                if (ModelRenderer.class.isAssignableFrom(field.getType()))
                    return (ModelRenderer) field.get(model);
            } catch (ReflectiveOperationException ignored) {
            }
            return null;
        }
    }
}
