package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一口径快照：所有通用枚举的当前取值集合，三端启动时拉一次即可")
public class CommonEnumsSnapshotVO implements Serializable {

    @Schema(description = "管线类型")
    private List<EnumValueVO> pipelineTypes;

    @Schema(description = "资产运行状态")
    private List<EnumValueVO> assetStatuses;

    @Schema(description = "优先级")
    private List<EnumValueVO> priorityLevels;

    @Schema(description = "告警类型")
    private List<EnumValueVO> alarmTypes;

    @Schema(description = "告警状态")
    private List<EnumValueVO> alarmStatuses;

    @Schema(description = "工单状态")
    private List<EnumValueVO> workOrderStatuses;

    @Schema(description = "工单来源")
    private List<EnumValueVO> workOrderSources;

    @Schema(description = "开挖审批状态")
    private List<EnumValueVO> excavationStatuses;

    @Schema(description = "资源类型")
    private List<EnumValueVO> resourceTypes;

    @Schema(description = "操作类型")
    private List<EnumValueVO> operationTypes;

    @Schema(description = "事件级别")
    private List<EnumValueVO> eventLevels;
}
