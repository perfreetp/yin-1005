package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("valve_operation_record")
@Schema(description = "阀门操作记录")
public class ValveOperationRecord extends BaseEntity {

    @Schema(description = "执行记录ID")
    private Long executionId;

    @Schema(description = "执行记录编号")
    private String executionCode;

    @Schema(description = "阀门ID")
    private Long valveId;

    @Schema(description = "阀门编号")
    private String valveCode;

    @Schema(description = "阀门名称")
    private String valveName;

    @Schema(description = "计划动作：1-关阀 2-节流 0-不动")
    private Integer plannedAction;

    @Schema(description = "实际动作：1-关阀 2-节流 0-不动")
    private Integer actualAction;

    @Schema(description = "计划执行顺序号")
    private Integer plannedOrderNo;

    @Schema(description = "实际执行顺序号")
    private Integer actualOrderNo;

    @Schema(description = "计划操作耗时（分钟）")
    private Integer plannedMinutes;

    @Schema(description = "实际操作耗时（分钟）")
    private Integer actualMinutes;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "是否可远程控制：0-否 1-是")
    private Integer remoteControllable;

    @Schema(description = "实际是否远程操作：0-否 1-是")
    private Integer actualIsRemote;

    @Schema(description = "是否成功：0-否 1-是")
    private Integer isSuccessful;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "操作前状态")
    private String beforeStatus;

    @Schema(description = "操作后状态")
    private String afterStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "现场确认人姓名")
    private String confirmerName;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "确认方式：1-远程 2-现场巡检 3-视频确认 4-传感器反馈")
    private Integer confirmMethod;

    @Schema(description = "现场照片ID，逗号分隔")
    private String photoIds;

    @Schema(description = "操作前压力")
    private BigDecimal beforePressure;

    @Schema(description = "操作后压力")
    private BigDecimal afterPressure;

    @Schema(description = "是否超时：0否1是")
    private Integer isTimeout;

    @Schema(description = "超时分钟数")
    private Integer timeoutMinutes;

    @Schema(description = "失败分类：1-阀门故障 2-操作失误 3-通讯故障 4-其他")
    private Integer failCategory;

    @Schema(description = "实际操作备注")
    private String actualRemark;
}
