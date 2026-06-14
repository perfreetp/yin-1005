package com.digitaltwin.pipeline.dto.trace;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "变更历史查询DTO")
public class ChangeHistoryQueryDTO extends PageQuery {

    @Schema(description = "业务类型：1-管线 2-阀门 3-井盖 4-传感器 5-隐患 6-工单 7-开挖申请 8-巡检记录")
    private Integer businessType;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "变更类型")
    private Integer changeType;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
