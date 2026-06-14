package com.digitaltwin.pipeline.dto.inspection;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "巡检记录查询DTO")
public class InspectionRecordQueryDTO extends PageQuery {

    @Schema(description = "路线ID")
    private Long routeId;

    @Schema(description = "巡检人ID")
    private Long inspectorId;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
