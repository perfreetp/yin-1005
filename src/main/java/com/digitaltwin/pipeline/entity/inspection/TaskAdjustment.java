package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_adjustment")
@Schema(description = "任务调整记录")
public class TaskAdjustment extends BaseEntity {

    @Schema(description = "变更日志ID")
    private Long changeLogId;

    @Schema(description = "原任务ID")
    private Long originalTaskId;

    @Schema(description = "原任务编号")
    private String originalTaskCode;

    @Schema(description = "调整类型：1-延期 2-提前 3-换人 4-换车 5-换班组 6-取消 7-优先级变更")
    private Integer adjustmentType;

    @Schema(description = "调整前值")
    private String beforeValue;

    @Schema(description = "调整后值")
    private String afterValue;

    @Schema(description = "调整原因")
    private String reason;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "是否已通知：0-否 1-是")
    private Integer isNotified;

    @Schema(description = "通知时间")
    private LocalDateTime notifyTime;
}
