package com.digitaltwin.pipeline.dto.linkage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "联动任务触发参数")
public class LinkedTaskTriggerDTO {

    @Schema(description = "触发来源类型：7-开挖审批 5-告警 6-隐患点 9-工单", required = true)
    private Integer sourceType;

    @Schema(description = "触发来源业务ID", required = true)
    private Long sourceId;

    @Schema(description = "任务类型：1-开挖高风险转巡检 2-告警触发抢修 3-隐患整改 4-审批联动待办 5-跨部门协同 6-综合指挥")
    private Integer taskType;

    @Schema(description = "任务标题，不传则根据来源自动生成")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "优先级：1-低 2-中 3-高 4-紧急，不传则自动判定")
    private Integer priority;

    @Schema(description = "手动指定承办部门ID")
    private Long undertakeDeptId;

    @Schema(description = "手动指定承办人ID")
    private Long undertakerId;

    @Schema(description = "协同部门ID列表")
    private List<Long> coDeptIds;

    @Schema(description = "经度，不传则从来源取")
    private BigDecimal lng;

    @Schema(description = "纬度，不传则从来源取")
    private BigDecimal lat;

    @Schema(description = "截止时间，不传则按优先级自动推算")
    private String deadline;

    @Schema(description = "是否同步生成巡检/抢修工单：0-否 1-是，默认1")
    private Integer autoGenerateWorkOrder;

    @Schema(description = "是否通知相关部门：0-否 1-是，默认1")
    private Integer autoNotifyDepartments;
}
