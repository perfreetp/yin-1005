package com.digitaltwin.pipeline.enums;

import lombok.Getter;

@Getter
public enum WorkOrderStatusEnum {

    PENDING_DISPATCH(1, "待派单", "PENDING_DISPATCH", 0),
    DISPATCHED(2, "已派单", "DISPATCHED", 20),
    PROCESSING(3, "处理中", "PROCESSING", 50),
    PENDING_ACCEPTANCE(4, "待验收", "PENDING_ACCEPTANCE", 80),
    COMPLETED(5, "已完成", "COMPLETED", 100),
    CANCELLED(6, "已取消", "CANCELLED", 0),
    REJECTED(7, "已驳回", "REJECTED", 20);

    private final Integer code;
    private final String label;
    private final String key;
    private final Integer progress;

    WorkOrderStatusEnum(Integer code, String label, String key, Integer progress) {
        this.code = code;
        this.label = label;
        this.key = key;
        this.progress = progress;
    }

    public static String getLabel(Integer code) {
        if (code == null) return "未知";
        for (WorkOrderStatusEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return "未知";
    }

    public static Integer getProgress(Integer code) {
        if (code == null) return 0;
        for (WorkOrderStatusEnum e : values()) {
            if (e.code.equals(code)) return e.progress;
        }
        return 0;
    }
}
