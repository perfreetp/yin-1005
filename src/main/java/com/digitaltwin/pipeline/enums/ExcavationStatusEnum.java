package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum ExcavationStatusEnum {

    DRAFT(0, "草稿", "DRAFT"),
    SUBMITTED(1, "已提交待审核", "SUBMITTED"),
    REVIEWING(2, "审核中", "REVIEWING"),
    APPROVED(3, "审核通过", "APPROVED"),
    REJECTED(4, "审核驳回", "REJECTED"),
    CONSTRUCTING(5, "施工中", "CONSTRUCTING"),
    COMPLETED(6, "施工完成", "COMPLETED"),
    ACCEPTED(7, "验收完成", "ACCEPTED"),
    CANCELLED(8, "已取消", "CANCELLED");

    private final Integer code;
    private final String label;
    private final String key;

    ExcavationStatusEnum(Integer code, String label, String key) {
        this.code = code;
        this.label = label;
        this.key = key;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (ExcavationStatusEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }
}
