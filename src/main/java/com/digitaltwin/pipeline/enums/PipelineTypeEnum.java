package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum PipelineTypeEnum {

    WATER(1, "给水", "WATER"),
    DRAINAGE(2, "排水", "DRAINAGE"),
    GAS(3, "燃气", "GAS"),
    POWER(4, "电力", "POWER"),
    COMMUNICATION(5, "通信", "COMMUNICATION"),
    HEATING(6, "热力", "HEATING"),
    INDUSTRIAL(7, "工业", "INDUSTRIAL");

    private final Integer code;
    private final String label;
    private final String key;

    PipelineTypeEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return UNKNOWN_LABEL;
        for (PipelineTypeEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return UNKNOWN_LABEL;
    }

    public static PipelineTypeEnum of(Integer code) {
        if (code == null) return null;
        for (PipelineTypeEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

    private static final String UNKNOWN_LABEL = "未知";
}
