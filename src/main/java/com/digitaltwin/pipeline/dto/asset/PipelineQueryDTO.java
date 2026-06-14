package com.digitaltwin.pipeline.dto.asset;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管线查询DTO")
public class PipelineQueryDTO extends PageQuery {

    @Schema(description = "管线类型")
    private Integer pipelineType;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关键词（编号/名称）")
    private String keyword;
}
