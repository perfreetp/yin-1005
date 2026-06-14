package com.digitaltwin.pipeline.entity.linkage;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("linked_task")
@Schema(description = "联动任务：跨审批/告警/巡检/工单的统一任务载体")
public class LinkedTask extends BaseEntity {

    @Schema(description = "联动任务编号，格式 LT-yyyyMMdd-xxxx")
    private String taskCode;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务类型：1-开挖高风险转巡检 2-告警触发抢修 3-隐患整改 4-审批联动待办 5-跨部门协同 6-综合指挥")
    private Integer taskType;

    @Schema(description = "触发来源类型：使用 ResourceTypeEnum")
    private Integer sourceType;

    @Schema(description = "触发来源业务ID")
    private Long sourceId;

    @Schema(description = "触发来源业务编号")
    private String sourceCode;

    @Schema(description = "优先级：1-低 2-中 3-高 4-紧急")
    private Integer priority;

    @Schema(description = "事件级别：1-提示 2-预警 3-事件 4-紧急 5-重大")
    private Integer eventLevel;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "涉及管线类型")
    private Integer pipelineType;

    @Schema(description = "承办部门ID")
    private Long undertakeDeptId;

    @Schema(description = "承办部门名称")
    private String undertakeDeptName;

    @Schema(description = "承办人ID")
    private Long undertakerId;

    @Schema(description = "承办人姓名")
    private String undertakerName;

    @Schema(description = "协同部门ID列表，逗号分隔")
    private String coDeptIds;

    @Schema(description = "协同部门名称列表，逗号分隔")
    private String coDeptNames;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "处置要求")
    private String disposalRequirement;

    @Schema(description = "截止时间")
    private String deadline;

    @Schema(description = "任务状态：1-待派单 2-处理中 3-待验收 4-已完成 5-已取消 6-已驳回")
    private Integer status;

    @Schema(description = "当前处理节点")
    private String currentNode;

    @Schema(description = "流程进度%")
    private Integer progress;

    @Schema(description = "关联工单ID，逗号分隔")
    private String relatedWorkOrderIds;

    @Schema(description = "关联巡检路线ID，逗号分隔")
    private String relatedRouteIds;

    @Schema(description = "已发送通知部门数")
    private Integer notifiedDeptCount;

    @Schema(description = "实际完成时间")
    private String actualFinishTime;

    @Schema(description = "总耗时(分钟)")
    private Integer usedMinutes;

    @Schema(description = "备注")
    private String remark;
}
