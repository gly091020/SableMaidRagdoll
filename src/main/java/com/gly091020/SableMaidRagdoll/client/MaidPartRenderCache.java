package com.gly091020.SableMaidRagdoll.client;

import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.SimpleBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.BedrockModelLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.gly091020.SableMaidRagdoll.block.MaidFairyPartBlockEntity;
import com.gly091020.SableMaidRagdoll.util.MaidModelHelper;
import com.gly091020.SableRagdollLib.resource.file.RagdollExpressions;
import com.gly091020.SableRagdollLib.resource.file.RagdollRenderData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * 按 ragdoll 定义文件缓存渲染所需的模型、部件列表和初始化动作，
 * 避免每帧重复进行模型查找、部件解析和表达式解析。
 */
public final class MaidPartRenderCache {
    private static final Map<ResourceLocation, Entry> CACHE = new HashMap<>();
    /** 妖精布娃娃按模型类型（成年/幼年）缓存 */
    private static final Map<MaidFairyPartBlockEntity.ModelType, FairyEntry> FAIRY_CACHE =
            new EnumMap<>(MaidFairyPartBlockEntity.ModelType.class);

    private MaidPartRenderCache() {
    }

    /**
     * 资源包重载后模型实例会失效，需要清空缓存。
     */
    public static void clear() {
        CACHE.clear();
        FAIRY_CACHE.clear();
    }

    public static Entry get(ResourceLocation defFile) {
        return CACHE.computeIfAbsent(defFile, MaidPartRenderCache::load);
    }

    /**
     * 获取妖精布娃娃的渲染缓存。
     */
    public static FairyEntry fairy(MaidFairyPartBlockEntity.ModelType modelType) {
        return FAIRY_CACHE.computeIfAbsent(modelType, MaidPartRenderCache::loadFairy);
    }

    private static Entry load(ResourceLocation defFile) {
        var modelID = defFile.getPath().replace("/", ":");
        var model = CustomPackLoader.MAID_MODELS.getModel(modelID).orElse(null);
        var info = CustomPackLoader.MAID_MODELS.getInfo(modelID).orElse(null);
        return new Entry(model, info);
    }

    private static FairyEntry loadFairy(MaidFairyPartBlockEntity.ModelType modelType) {
        var model = switch (modelType) {
            case BABY -> BedrockModelLoader.getModel(BedrockModelLoader.BABY_MAID_FAIRY);
            case NEW -> BedrockModelLoader.getModel(BedrockModelLoader.NEW_MAID_FAIRY);
        };
        return new FairyEntry(model);
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
        /** partName -> 该部位需要渲染的持物信息 */
        private @Nullable Map<String, HandRender> handCache;
        /** BedrockPart 实例 -> 部件名 */
        private @Nullable Map<BedrockPart, String> partNameMap;

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

        /**
         * 获取该部位需要渲染手持物品的信息，没有则返回 null。
         */
        @Nullable
        public HandRender handRender(String partName) {
            if (handCache == null) {
                handCache = new HashMap<>();
                addHand(handCache, model.getLeftArm(), InteractionHand.OFF_HAND);
                addHand(handCache, model.getRightArm(), InteractionHand.MAIN_HAND);
            }
            return handCache.get(partName);
        }

        private void addHand(Map<String, HandRender> map, @Nullable BedrockPart hand, InteractionHand interactionHand) {
            if (hand == null) return;
            // 手臂自身以及它的祖先部位都可以作为持物载体
            for (var part = hand; part != null; part = part.parent) {
                var name = partNameOf(part);
                if (name != null) {
                    map.putIfAbsent(name, new HandRender(hand, part, interactionHand));
                }
            }
        }

        @Nullable
        private String partNameOf(BedrockPart part) {
            if (partNameMap == null) {
                var map = new IdentityHashMap<BedrockPart, String>();
                model.getModelMap().forEach((name, p) -> map.put(p, name));
                partNameMap = map;
            }
            return partNameMap.get(part);
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
     * 妖精布娃娃的渲染缓存：模型固定来自 BedrockModelLoader，
     * 这里预存部件快照、部件名映射和按 (defFile, partName) 解析好的渲染列表。
     */
    public static final class FairyEntry {
        private final @Nullable SimpleBedrockModel<?> model;
        /** 模型全部部件的快照，用于每帧重置状态 */
        private final List<BedrockPart> allParts;
        /** reset 时需要隐藏的部件快照 */
        private final List<BedrockPart> ignoreParts;
        /** partName -> BedrockPart */
        private final Map<String, BedrockPart> partMap;
        /** defFile -> partName -> 该部位需要渲染的模型部件列表 */
        private final Map<ResourceLocation, Map<String, List<PartRender>>> partsCache = new HashMap<>();

        private FairyEntry(@Nullable SimpleBedrockModel<?> model) {
            this.model = model;
            if (model == null) {
                this.allParts = List.of();
                this.ignoreParts = List.of();
                this.partMap = Map.of();
            } else {
                this.allParts = List.copyOf(model.getModelMap().values());
                this.ignoreParts = MaidModelHelper.IGNORE_PART.stream()
                        .map(name -> model.getModelMap().get(name))
                        .filter(part -> part != null)
                        .toList();
                this.partMap = Map.copyOf(model.getModelMap());
            }
        }

        public @Nullable SimpleBedrockModel<?> model() {
            return model;
        }

        /**
         * 重置模型到初始状态，并保持眨眼部位可见（等价于 resetModel + showPart("blink")）。
         */
        public void reset() {
            for (BedrockPart part : allParts) {
                MaidModelHelper.resetModel(part);
            }
            for (BedrockPart part : ignoreParts) {
                MaidModelHelper.hidePart(part);
            }
            var blink = partMap.get("blink");
            if (blink != null) {
                blink.visible = true;
            }
        }

        /**
         * 获取该 ragdoll 定义文件中某部位需要渲染的模型部件列表，按 defFile + partName 缓存，
         * 避免不同定义文件共用同一部位名时解析结果串用。
         */
        public List<PartRender> parts(ResourceLocation defFile, String partName, List<RagdollRenderData.EveryPart> renderParts) {
            return partsCache.computeIfAbsent(defFile, f -> new HashMap<>())
                    .computeIfAbsent(partName, p -> {
                        var resolved = new ArrayList<PartRender>();
                        for (RagdollRenderData.EveryPart part : renderParts) {
                            var bedrockPart = partMap.get(part.partName());
                            if (bedrockPart != null) {
                                resolved.add(new PartRender(bedrockPart, part.flatChild()));
                            }
                        }
                        return List.copyOf(resolved);
                    });
        }
    }

    /**
     * 需要渲染的模型部件，flatChild 为 true 时只画部件本身，不递归子部件（与 geo 渲染一致）。
     */
    public record PartRender(BedrockPart part, boolean flatChild) {
    }

    /**
     * 手持物品渲染信息，hand 为实际手臂部件，parent 为与 ragdoll 部位匹配的部件。
     */
    public record HandRender(BedrockPart hand, BedrockPart parent, InteractionHand interactionHand) {
        public boolean leftHand() {
            return interactionHand == InteractionHand.OFF_HAND;
        }
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
