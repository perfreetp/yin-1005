package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_timeline_point")
@Schema(description = "事件时间轴节点（用于态势和回放）")
public class EventTimelinePoint extends BaseEntity {

    @Schema(description = "事件ID")
    private Long eventId;

    @Schema(description = "事件编号")
    private String eventCode;

    @Schema(description = "时间节点类型：1-事件发现 2-告警触发 3-审批提交 4-派单通知 5-现场到达 6-关阀作业 7-开挖作业 8-抢修作业 9-恢复作业 10-处置完成 11-验收 12-事件结束 13-关键节点")
    private Integer pointType;

    @Schema(description = "节点标题")
    private String title;

    @Schema(description = "节点详情描述")
    private String description;

    @Schema(description = "发生时间")
    private String occurTime;

    @Schema(description = "持续时长(分钟)，节点动作的耗时")
    private Integer durationMinutes;

    @Schema(description = "关联业务资源类型")
    private Integer resourceType;

    @Schema(description = "关联业务资源ID")
    private Long resourceId;

    @Schema(description = "关联业务编号")
    private String resourceCode;

    @Schema(description = "经度，便于地图打点")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "操作人/部门")
    private String operatorName;

    @Schema(description = "节点标签：key1,key2")
    private String tags;

    @Schema(description = "节点等级影响：1-轻微 2-一般 3-重要 4-严重")
    private Integer importance;

    @Schema(description = "附件JSON")
    private String attachments;
}
