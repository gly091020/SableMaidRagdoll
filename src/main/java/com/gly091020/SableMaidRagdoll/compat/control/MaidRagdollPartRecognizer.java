package com.gly091020.SableMaidRagdoll.compat.control;

import com.gly091020.SableRagdollLib.api.control.IRagdollPartRecognizer;
import com.gly091020.SableRagdollLib.api.control.PartRole;
import com.gly091020.SableRagdollLib.api.control.RagdollPartNameRules;
import com.gly091020.SableRagdollLib.resource.file.RagdollDefFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 附属模组的部位识别补充：完全按名称匹配（名称全部小写后按关键字判断），
 * 例如同时包含 "left" 与 "arm" 即识别为左手。
 * <p>
 * 在库的通用识别器之后注册，用命名规则修正通用识别器对女仆/妖精等
 * 特殊模型的几何兜底推断。
 */
public class MaidRagdollPartRecognizer implements IRagdollPartRecognizer {
    @Override
    public Map<PartRole, String> recognize(RagdollDefFile defFile, Map<PartRole, String> current) {
        Map<PartRole, String> result = new HashMap<>(current);
        for (String part : defFile.allParts()) {
            PartRole role = RagdollPartNameRules.match(part);
            if (role != null) {
                result.put(role, part);
            }
        }
        return result;
    }
}
