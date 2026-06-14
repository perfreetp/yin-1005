package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum PriorityLevelEnum {

    LOW(1, "低", "LOW", "#22c55e"),
    MEDIUM(2, "中", "MEDIUM", "#eab308"),
    HIGH(3, "高", "HIGH", "#f97316"),
    CRITICAL(4, "紧急", "CRITICAL", "#ef4444");

    private final Integer code;
    private final String label;
    private final String key;
    private final String color;

    PriorityLevelEnum(Integer code, String label, String key, String color) {
        this.code = code;
        this.label = label;
        this.key = key;
        this.color = color;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (PriorityLevelEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }

    public static String getColor(Integer code) {
        if (code == null) return "#94a3b8";
        for (PriorityLevelEnum e : values()) {
            if (e.code.equals(code)) return e.color;
        }
        return "#94a3b8";
    }
}
