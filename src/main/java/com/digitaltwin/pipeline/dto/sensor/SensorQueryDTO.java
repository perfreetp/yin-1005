package com.digitaltwin.pipeline.dto.sensor;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "传感器查询DTO")
public class SensorQueryDTO extends PageQuery {

    @Schema(description = "传感器类型")
    private Integer sensorType;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "关键词（编号/名称）")
    private String keyword;
}
