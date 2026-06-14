package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum AssetStatusEnum {

    NORMAL(1, "正常运行", "NORMAL"),
    UNDER_REPAIR(2, "维修中", "UNDER_REPAIR"),
    STOPPED(3, "停用", "STOPPED"),
    ABANDONED(4, "废弃", "ABANDONED"),
    PENDING_INSPECTION(5, "待巡检", "PENDING_INSPECTION");

    private final Integer code;
    private final String label;
    private final String key;

    AssetStatusEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (AssetStatusEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }

    public static AssetStatusEnum of(Integer code) {
        if (code == null) return null;
        for (AssetStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
