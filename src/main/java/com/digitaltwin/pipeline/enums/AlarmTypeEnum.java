package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum AlarmTypeEnum {

    PRESSURE(1, "压力超限", "PRESSURE"),
    LEVEL(2, "液位超限", "LEVEL"),
    FLOW(3, "流量异常", "FLOW"),
    TEMPERATURE(4, "温度异常", "TEMPERATURE"),
    VIBRATION(5, "振动异常", "VIBRATION"),
    GAS_LEAK(6, "气体泄漏", "GAS_LEAK"),
    WATER_QUALITY(7, "水质异常", "WATER_QUALITY"),
    OFFLINE(8, "设备离线", "OFFLINE"),
    OTHER(9, "其他异常", "OTHER");

    private final Integer code;
    private final String label;
    private final String key;

    AlarmTypeEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (AlarmTypeEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }
}
