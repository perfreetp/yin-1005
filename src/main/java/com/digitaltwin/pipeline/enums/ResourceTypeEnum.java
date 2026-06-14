package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum ResourceTypeEnum {

    PIPELINE(1, "管线档案", "PIPELINE"),
    VALVE(2, "阀门档案", "VALVE"),
    MANHOLE(3, "井盖档案", "MANHOLE"),
    SENSOR(4, "传感器", "SENSOR"),
    ALARM(5, "告警记录", "ALARM"),
    HAZARD(6, "隐患点", "HAZARD"),
    EXCAVATION(7, "开挖申请", "EXCAVATION"),
    INSPECTION(8, "巡检记录", "INSPECTION"),
    WORK_ORDER(9, "维修工单", "WORK_ORDER"),
    DEPARTMENT(10, "部门", "DEPARTMENT"),
    USER(11, "用户", "USER"),
    SHARING_SCOPE(12, "共享权限", "SHARING_SCOPE"),
    INSPECTION_ROUTE(13, "巡检路线", "INSPECTION_ROUTE"),
    LINKED_TASK(14, "联动任务", "LINKED_TASK"),
    EVENT_INCIDENT(15, "事件记录", "EVENT_INCIDENT");

    private final Integer code;
    private final String label;
    private final String key;

    ResourceTypeEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (ResourceTypeEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }
}
