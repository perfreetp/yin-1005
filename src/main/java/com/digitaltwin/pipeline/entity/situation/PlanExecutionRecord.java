package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_execution_record")
@Schema(description = "方案执行记录")
public class PlanExecutionRecord extends BaseEntity {

    @Schema(description = "执行记录编号，格式 PER-yyyyMMdd-xxxx")
    private String recordCode;

    @Schema(description = "告警ID")
    private Long alarmId;

    @Schema(description = "告警编号")
    private String alarmCode;

    @Schema(description = "事件ID")
    private Long eventId;

    @Schema(description = "事件编号")
    private String eventCode;

    @Schema(description = "方案ID")
    private Long planId;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "策略类型：1-最小影响用户 2-最快恢复 3-最省资源 4-最安全")
    private Integer strategyType;

    @Schema(description = "执行状态：1-待执行 2-执行中 3-已完成 4-部分成功 5-执行失败")
    private Integer status;

    @Schema(description = "执行人")
    private String executor;

    @Schema(description = "执行部门")
    private String executorDept;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "实际总耗时（分钟）")
    private Integer totalUsedMinutes;

    @Schema(description = "预估总耗时（分钟）")
    private Integer estimatedTotalMinutes;

    @Schema(description = "实际影响用户数")
    private Integer actualAffectedUsers;

    @Schema(description = "预估影响用户数")
    private Integer estimatedAffectedUsers;

    @Schema(description = "实际影响企业数")
    private Integer actualAffectedEnterprises;

    @Schema(description = "预估影响企业数")
    private Integer estimatedAffectedEnterprises;

    @Schema(description = "时间偏差（分钟），正慢负快")
    private Integer deviationMinutes;

    @Schema(description = "用户影响偏差")
    private Integer deviationUsers;

    @Schema(description = "准确度评分（0-100）")
    private Integer accuracyScore;

    @Schema(description = "备注")
    private String remark;
}
