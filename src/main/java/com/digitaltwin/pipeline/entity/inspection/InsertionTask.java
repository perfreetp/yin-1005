package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("insertion_task")
@Schema(description = "插单任务")
public class InsertionTask extends BaseEntity {

    @Schema(description = "插单编号")
    private String insertionCode;

    @Schema(description = "来源类型：1-告警 2-隐患 3-工单 4-开挖 5-上级指令")
    private Integer sourceType;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "来源编号")
    private String sourceCode;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "优先级：1-低 2-中 3-高 4-紧急")
    private Integer priority;

    @Schema(description = "管线类型")
    private Integer pipelineType;

    @Schema(description = "区域编码")
    private String areaCode;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "预计耗时(分钟)")
    private Integer estimatedMinutes;

    @Schema(description = "需要的技能，逗号分隔")
    private String requiredSkills;

    @Schema(description = "需要的车辆类型")
    private Integer requiredVehicleType;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "指派班组ID")
    private Long assigneeTeamId;

    @Schema(description = "指派班组名称")
    private String assigneeTeamName;

    @Schema(description = "状态：0-待排 1-已排班 2-已取消 3-已完成")
    private Integer status;

    @Schema(description = "插单原因")
    private String insertReason;

    @Schema(description = "操作人姓名")
    private String operatorName;
}
