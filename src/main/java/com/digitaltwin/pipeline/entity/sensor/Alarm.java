package com.digitaltwin.pipeline.entity.sensor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_alarm")
@Schema(description = "告警记录")
public class Alarm extends BaseEntity {

    @Schema(description = "告警编号")
    private String alarmCode;

    @Schema(description = "告警类型：1-压力超限 2-液位超限 3-流量异常 4-温度异常 5-振动异常 6-气体泄漏 7-水质异常 8-设备离线 9-其他")
    private Integer alarmType;

    @Schema(description = "告警级别：1-低 2-中 3-高 4-紧急")
    private Integer alarmLevel;

    @Schema(description = "告警标题")
    private String title;

    @Schema(description = "告警内容描述")
    private String content;

    @Schema(description = "触发传感器ID")
    private Long sensorId;

    @Schema(description = "触发传感器编号")
    private String sensorCode;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "关联管线编号")
    private String pipelineCode;

    @Schema(description = "告警位置经度")
    private BigDecimal lng;

    @Schema(description = "告警位置纬度")
    private BigDecimal lat;

    @Schema(description = "告警位置描述")
    private String location;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "告警阈值")
    private BigDecimal thresholdValue;

    @Schema(description = "实际值")
    private BigDecimal actualValue;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "告警触发时间")
    private String alarmTime;

    @Schema(description = "告警状态：1-待处理 2-处理中 3-已处置 4-已忽略")
    private Integer status;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理时间")
    private String handleTime;

    @Schema(description = "处理结果说明")
    private String handleResult;

    @Schema(description = "关联隐患点ID")
    private Long hazardId;

    @Schema(description = "关联工单ID")
    private Long workOrderId;
}
