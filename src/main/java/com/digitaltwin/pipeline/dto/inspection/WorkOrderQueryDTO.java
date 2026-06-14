package com.digitaltwin.pipeline.dto.inspection;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工单查询DTO")
public class WorkOrderQueryDTO extends PageQuery {

    @Schema(description = "工单类型")
    private Integer orderType;

    @Schema(description = "工单来源")
    private Integer orderSource;

    @Schema(description = "紧急程度")
    private Integer urgency;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "承办人")
    private String undertaker;

    @Schema(description = "关键词")
    private String keyword;
}
