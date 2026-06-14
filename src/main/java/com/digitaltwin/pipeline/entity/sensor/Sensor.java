package com.digitaltwin.pipeline.entity.sensor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_sensor")
@Schema(description = "传感器实体")
public class Sensor extends BaseEntity {

    @Schema(description = "传感器编号")
    private String sensorCode;

    @Schema(description = "传感器名称")
    private String sensorName;

    @Schema(description = "传感器类型：1-压力 2-液位 3-流量 4-温度 5-振动 6-气体浓度 7-水质")
    private Integer sensorType;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "厂商")
    private String manufacturer;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "安装位置描述")
    private String installLocation;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "关联管线编号")
    private String pipelineCode;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "量程下限")
    private BigDecimal rangeMin;

    @Schema(description = "量程上限")
    private BigDecimal rangeMax;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "告警阈值上限")
    private BigDecimal alarmUpper;

    @Schema(description = "告警阈值下限")
    private BigDecimal alarmLower;

    @Schema(description = "采集频率(秒)")
    private Integer collectInterval;

    @Schema(description = "安装日期")
    private String installDate;

    @Schema(description = "运维单位")
    private String maintenanceUnit;

    @Schema(description = "状态：1-在线 2-离线 3-故障 4-维护中")
    private Integer status;

    @Schema(description = "最新读数")
    private BigDecimal lastValue;

    @Schema(description = "最新读数时间")
    private String lastReadTime;

    @Schema(description = "备注")
    private String remark;
}
