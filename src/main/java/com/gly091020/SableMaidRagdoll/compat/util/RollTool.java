package com.gly091020.SableMaidRagdoll.compat.util;

import com.github.tartaricacid.touhoulittlemaid.ai.agent.tool.ITool;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.BoolParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.gly091020.SableRagdollLib.entity.PartSeat;
import com.mojang.serialization.Codec;

public class RollTool implements ITool<Boolean> {
    public static final String TOOL_ID = "maid_ragdoll_roll";
    private static final String TOOL_DESC = """
            此工具用于控制角色躺倒翻滚或站立。
            """.trim();
    private static final String TOOL_DISABLED_DESC = "此工具目前不可用。";
    private static final String BOOL_ID = "mode";
    private static final String BOOL_DESC = """
            true: 躺倒翻滚，false: 站起来
            """.trim();
    @Override
    public String id() {
        return TOOL_ID;
    }

    @Override
    public String summary(EntityMaid maid) {
        if(!MaidRollManager.canRoll(maid))return TOOL_DISABLED_DESC;
        return TOOL_DESC;
    }

    @Override
    public Parameter parameters(ObjectParameter root, EntityMaid maid) {
        BoolParameter boolParameter = BoolParameter.create();
        boolParameter.setDescription(BOOL_DESC);
        root.addProperties(BOOL_ID, boolParameter);
        return root;
    }

    @Override
    public Codec<Boolean> codec() {
        return Codec.BOOL.fieldOf("mode").codec();
    }

    @Override
    public LLMCallback onCall(String toolCallId, Boolean b, LLMCallback callback) {
        EntityMaid maid = callback.getMaid();
        if(b){
            if(!MaidRollManager.canRoll(maid))return callback.addToolResult(TOOL_DISABLED_DESC, toolCallId);
            MaidRollManager.startRolling(maid);
            return callback.addToolResult("已成功躺倒并翻滚", toolCallId);
        }else{
            if(maid.getVehicle() instanceof PartSeat partSeat)
                partSeat.ejectPassengers();
            return callback.addToolResult("已成功站起来", toolCallId);
        }
    }
}
