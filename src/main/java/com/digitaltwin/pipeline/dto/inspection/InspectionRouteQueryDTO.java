package com.digitaltwin.pipeline.dto.inspection;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "巡检路线查询DTO")
public class InspectionRouteQueryDTO extends PageQuery {

    @Schema(description = "路线类型")
    private Integer routeType;

    @Schema(description = "管线类型")
    private Integer pipelineType;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关键词")
    private String keyword;
}
