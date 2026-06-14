package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum OperationTypeEnum {

    CREATE(1, "新增", "CREATE"),
    UPDATE(2, "修改", "UPDATE"),
    DELETE(3, "删除", "DELETE"),
    STATUS_CHANGE(4, "状态变更", "STATUS_CHANGE"),
    FLOW_TRANSITION(5, "流程流转", "FLOW_TRANSITION"),
    LINKAGE(6, "联动触发", "LINKAGE"),
    NOTIFY(7, "通知发送", "NOTIFY"),
    APPROVAL(8, "审批", "APPROVAL"),
    DISPATCH(9, "任务派单", "DISPATCH");

    private final Integer code;
    private final String label;
    private final String key;

    OperationTypeEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (OperationTypeEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }
}
