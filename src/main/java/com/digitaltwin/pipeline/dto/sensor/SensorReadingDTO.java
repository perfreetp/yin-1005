package com.digitaltwin.pipeline.dto.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "传感器读数上报DTO")
public class SensorReadingDTO {

    @NotNull(message = "传感器ID不能为空")
    @Schema(description = "传感器ID")
    private Long sensorId;

    @NotNull(message = "读数不能为空")
    @Schema(description = "读数")
    private BigDecimal readingValue;

    @Schema(description = "采集时间")
    private String collectTime;
}
