package com.digitaltwin.pipeline.entity.sensor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_sensor_reading")
@Schema(description = "传感器读数记录")
public class SensorReading extends BaseEntity {

    @Schema(description = "传感器ID")
    private Long sensorId;

    @Schema(description = "传感器编号")
    private String sensorCode;

    @Schema(description = "传感器类型：1-压力 2-液位 3-流量 4-温度 5-振动 6-气体浓度 7-水质")
    private Integer sensorType;

    @Schema(description = "读数")
    private BigDecimal readingValue;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "采集时间")
    private String collectTime;

    @Schema(description = "是否告警：0-否 1-是")
    private Integer isAlarm;

    @Schema(description = "告警级别：1-低 2-中 3-高 4-紧急")
    private Integer alarmLevel;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "所属区域编码")
    private String areaCode;
}
