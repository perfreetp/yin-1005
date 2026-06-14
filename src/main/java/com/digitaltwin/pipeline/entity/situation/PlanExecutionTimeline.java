package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_execution_timeline")
@Schema(description = "执行时间轴节点")
public class PlanExecutionTimeline extends BaseEntity {

    @Schema(description = "执行记录ID")
    private Long executionId;

    @Schema(description = "执行记录编号")
    private String executionCode;

    @Schema(description = "节点类型：1-方案选定 2-派单 3-到达现场 4-开始关阀 5-完成关阀 6-开始维修 7-完成维修 8-开始恢复 9-恢复完成 10-意外事件")
    private Integer pointType;

    @Schema(description = "节点标题")
    private String title;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "发生时间")
    private LocalDateTime occurTime;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "是否关键节点：0-否 1-是")
    private Integer isKeyNode;

    @Schema(description = "关联资源类型")
    private String resourceType;

    @Schema(description = "关联资源ID")
    private Long resourceId;

    @Schema(description = "关联资源编号")
    private String resourceCode;
}
