package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum AlarmStatusEnum {

    PENDING(1, "待处置", "PENDING"),
    HANDLING(2, "处置中", "HANDLING"),
    RESOLVED(3, "已处置", "RESOLVED"),
    CONFIRMED(4, "已确认", "CONFIRMED"),
    FALSE_ALARM(5, "误报", "FALSE_ALARM");

    private final Integer code;
    private final String label;
    private final String key;

    AlarmStatusEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (AlarmStatusEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }
}
