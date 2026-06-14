package com.digitaltwin.pipeline.dto.sensor;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "隐患点查询DTO")
public class HazardQueryDTO extends PageQuery {

    @Schema(description = "隐患类型")
    private Integer hazardType;

    @Schema(description = "风险等级")
    private Integer riskLevel;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关键词")
    private String keyword;
}
