package com.digitaltwin.pipeline.dto.sensor;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "传感器读数查询DTO")
public class SensorReadingQueryDTO extends PageQuery {

    @Schema(description = "传感器ID")
    private Long sensorId;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
