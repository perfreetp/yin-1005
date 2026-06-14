package com.digitaltwin.pipeline.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    PIPELINE_NOT_FOUND(1001, "管线不存在"),
    VALVE_NOT_FOUND(1002, "阀门不存在"),
    MANHOLE_NOT_FOUND(1003, "井盖不存在"),
    SENSOR_NOT_FOUND(1004, "传感器不存在"),
    HAZARD_NOT_FOUND(1005, "隐患点不存在"),
    WORKORDER_NOT_FOUND(1006, "工单不存在"),
    INSPECTION_NOT_FOUND(1007, "巡检记录不存在"),
    EXCAVATION_NOT_FOUND(1008, "开挖申请不存在"),

    PIPELINE_CONFLICT(2001, "存在管线冲突"),
    STATUS_ERROR(2002, "状态错误"),
    PERMISSION_DENIED(2003, "无权限访问该区域或管线类型");

    private final Integer code;
    private final String message;
}
