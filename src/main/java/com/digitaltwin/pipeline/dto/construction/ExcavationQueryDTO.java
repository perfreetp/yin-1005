package com.digitaltwin.pipeline.dto.construction;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "开挖申请查询DTO")
public class ExcavationQueryDTO extends PageQuery {

    @Schema(description = "开挖类型")
    private Integer excavationType;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "申请单位")
    private String applicantUnit;

    @Schema(description = "关键词")
    private String keyword;
}
