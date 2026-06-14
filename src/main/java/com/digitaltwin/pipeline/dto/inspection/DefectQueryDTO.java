package com.digitaltwin.pipeline.dto.inspection;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "缺陷上报查询DTO")
public class DefectQueryDTO extends PageQuery {

    @Schema(description = "缺陷类型")
    private Integer defectType;

    @Schema(description = "缺陷等级")
    private Integer defectLevel;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "上报来源")
    private Integer reportSource;

    @Schema(description = "关键词")
    private String keyword;
}
