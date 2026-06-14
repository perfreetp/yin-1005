package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum WorkOrderSourceEnum {

    ALARM(1, "告警触发", "ALARM"),
    HAZARD(2, "隐患排查", "HAZARD"),
    DEFECT(3, "缺陷上报", "DEFECT"),
    INSPECTION(4, "巡检发现", "INSPECTION"),
    EXCAVATION(5, "开挖施工", "EXCAVATION"),
    APPROVAL_LINK(6, "审批联动", "APPROVAL_LINK"),
    OTHER(7, "其他来源", "OTHER");

    private final Integer code;
    private final String label;
    private final String key;

    WorkOrderSourceEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (WorkOrderSourceEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }
}
