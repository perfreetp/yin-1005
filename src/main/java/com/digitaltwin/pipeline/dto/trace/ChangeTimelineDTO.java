package com.digitaltwin.pipeline.dto.trace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "变更历史时间线结果")
public class ChangeTimelineDTO {

    @Schema(description = "资源类型：1-管线 2-阀门 3-井盖 4-传感器 5-告警 6-隐患点 7-开挖申请 8-巡检记录 9-工单 10-部门 11-用户 12-共享权限")
    private Integer resourceType;

    @Schema(description = "资源类型名称")
    private String resourceTypeName;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "资源名称/编号")
    private String resourceName;

    @Schema(description = "总变更次数")
    private Integer totalChangeCount;

    @Schema(description = "近7天变更次数")
    private Integer last7DaysCount;

    @Schema(description = "近30天变更次数")
    private Integer last30DaysCount;

    @Schema(description = "变更操作人员TOP5")
    private List<OperatorStat> topOperators;

    @Schema(description = "时间线明细（按时间倒序）")
    private List<TimelineItem> items;

    @Data
    @Schema(description = "操作人员统计")
    public static class OperatorStat {
        @Schema(description = "操作人名称")
        private String operatorName;
        @Schema(description = "操作次数")
        private Integer count;
    }

    @Data
    @Schema(description = "时间线条目")
    public static class TimelineItem {
        @Schema(description = "变更记录ID")
        private Long changeId;
        @Schema(description = "变更时间")
        private LocalDateTime changeTime;
        @Schema(description = "操作类型：1-新增 2-修改 3-删除 4-状态变更 5-流程流转")
        private Integer operationType;
        @Schema(description = "操作类型名称")
        private String operationTypeName;
        @Schema(description = "操作说明")
        private String operation;
        @Schema(description = "操作人ID")
        private Long operatorId;
        @Schema(description = "操作人姓名")
        private String operatorName;
        @Schema(description = "操作人所属部门")
        private String operatorDept;
        @Schema(description = "变更前摘要")
        private String beforeSummary;
        @Schema(description = "变更后摘要")
        private String afterSummary;
        @Schema(description = "差异字段列表")
        private List<FieldDiff> fieldDiffs;
        @Schema(description = "IP地址")
        private String ipAddress;
        @Schema(description = "备注/原因")
        private String remark;
    }

    @Data
    @Schema(description = "字段差异对比")
    public static class FieldDiff {
        @Schema(description = "字段名")
        private String fieldName;
        @Schema(description = "字段显示名")
        private String displayName;
        @Schema(description = "变更前值")
        private String beforeValue;
        @Schema(description = "变更后值")
        private String afterValue;
        @Schema(description = "是否关键变更：0-否 1-是")
        private Integer isKey;
    }
}
