package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum EventLevelEnum {

    NOTIFICATION(1, "提示", "NOTIFICATION", "#06b6d4"),
    WARNING(2, "预警", "WARNING", "#eab308"),
    INCIDENT(3, "事件", "INCIDENT", "#f97316"),
    EMERGENCY(4, "紧急事件", "EMERGENCY", "#ef4444"),
    DISASTER(5, "重大事件", "DISASTER", "#b91c1c");

    private final Integer code;
    private final String label;
    private final String key;
    private final String color;

    EventLevelEnum(Integer code, String label, String key, String color) {
        this.code = code;
        this.label = label;
        this.key = key;
        this.color = color;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (EventLevelEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }

    public static String getColor(Integer code) {
        if (code == null) return "#94a3b8";
        for (EventLevelEnum e : values()) {
            if (e.code.equals(code)) return e.color;
        }
        return "#94a3b8";
    }
}
