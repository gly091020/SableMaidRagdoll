package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.gly091020.SableRagdollLib.resource.file.RagdollExpressions;
import com.gly091020.SableRagdollLib.resource.file.RagdollRenderData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 按 ragdoll 定义文件缓存渲染所需的模型、部件列表和初始化动作，
 * 避免每帧重复进行模型查找、部件解析和表达式解析。
 */
public final class MaidPartRenderCache {
    private static final Map<ResourceLocation, Entry> CACHE = new HashMap<>();

    private MaidPartRenderCache() {
    }

    /**
     * 资源包重载后模型实例会失效，需要清空缓存。
     */
    public static void clear() {
        CACHE.clear();
    }

    public static Entry get(ResourceLocation defFile) {
        return CACHE.computeIfAbsent(defFile, MaidPartRenderCache::load);
    }

    private static Entry load(ResourceLocation defFile) {
        var modelID = defFile.getPath().replace("/", ":");
        var model = CustomPackLoader.MAID_MODELS.getModel(modelID).orElse(null);
        var info = CustomPackLoader.MAID_MODELS.getInfo(modelID).orElse(null);
        return new Entry(model, info);
    }

    public static final class Entry {
        private final @Nullable BedrockModel<Mob> model;
        private final @Nullable MaidModelInfo info;
        /** 模型全部部件的快照，用于每帧重置状态 */
        private final List<BedrockPart> allParts;
        /** reset 时需要隐藏的部件快照 */
        private final List<BedrockPart> ignoreParts;
        /** partName -> 该部位需要渲染的模型部件列表 */
        private final Map<String, List<PartRender>> partsCache = new HashMap<>();
        /** 解析后的 init 动作 */
        private @Nullable List<InitAction> initActions;

        private Entry(@Nullable BedrockModel<Mob> model, @Nullable MaidModelInfo info) {
            this.model = model;
            this.info = info;
            if (model == null) {
                this.allParts = List.of();
                this.ignoreParts = List.of();
            } else {
                this.allParts = List.copyOf(model.getModelMap().values());
                this.ignoreParts = MaidModelHelper.IGNORE_PART.stream()
                        .map(name -> model.getModelMap().get(name))
                        .filter(part -> part != null)
                        .toList();
            }
        }

        public @Nullable BedrockModel<Mob> model() {
            return model;
        }

        public @Nullable MaidModelInfo info() {
            return info;
        }

        public ResourceLocation texture() {
            return info.getTexture();
        }

        public float scale() {
            return info.getRenderEntityScale();
        }

        /**
         * 重置模型到初始状态，等价于 MaidModelHelper.resetModel(model)。
         */
        public void reset() {
            for (BedrockPart part : allParts) {
                MaidModelHelper.resetModel(part);
            }
            for (BedrockPart part : ignoreParts) {
                MaidModelHelper.hidePart(part);
            }
        }

        /**
         * 应用 init 表达式，同一表达式只会解析一次。
         */
        public void applyInit(RagdollExpressions expressions) {
            if (initActions == null) {
                initActions = resolveInitActions(expressions);
            }
            for (InitAction action : initActions) {
                action.apply();
            }
        }

        /**
         * 获取该部位需要渲染的模型部件列表，按 partName 缓存。
         */
        public List<PartRender> parts(String partName, List<RagdollRenderData.EveryPart> renderParts) {
            return partsCache.computeIfAbsent(partName, p -> {
                var resolved = new ArrayList<PartRender>();
                for (RagdollRenderData.EveryPart part : renderParts) {
                    var bedrockPart = model.getModelMap().get(part.partName());
                    if (bedrockPart != null) {
                        resolved.add(new PartRender(bedrockPart, part.flatChild()));
                    }
                }
                return List.copyOf(resolved);
            });
        }

        private List<InitAction> resolveInitActions(RagdollExpressions expressions) {
            var actions = new ArrayList<InitAction>();
            expressions.getExpression("init").ifPresent(expressionMap ->
                    expressionMap.forEach((partName, expression) -> actions.add(new InitAction(
                            model.getModelMap().get(partName),
                            switch (expression.actionType()) {
                                case "hide" -> false;
                                case "show" -> true;
                                default -> null;
                            },
                            (float) expression.transform().x,
                            (float) expression.transform().y,
                            (float) expression.transform().z,
                            (float) Math.toRadians(expression.rotation().x),
                            (float) Math.toRadians(expression.rotation().y),
                            (float) Math.toRadians(expression.rotation().z)
                    ))));
            return List.copyOf(actions);
        }
    }

    /**
     * 需要渲染的模型部件，flatChild 为 true 时只画部件本身，不递归子部件（与 geo 渲染一致）。
     */
    public record PartRender(BedrockPart part, boolean flatChild) {
    }

    private record InitAction(
            @Nullable BedrockPart part,
            @Nullable Boolean visible,
            float offsetX, float offsetY, float offsetZ,
            float xRot, float yRot, float zRot) {

        void apply() {
            if (part == null) {
                return;
            }
            if (visible != null) {
                part.visible = visible;
            }
            part.offsetX = offsetX;
            part.offsetY = offsetY;
            part.offsetZ = offsetZ;
            part.xRot = xRot;
            part.yRot = yRot;
            part.zRot = zRot;
        }
    }
}
