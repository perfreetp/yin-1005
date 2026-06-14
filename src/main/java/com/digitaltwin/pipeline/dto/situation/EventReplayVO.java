package com.digitaltwin.pipeline.dto.situation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "事件详情+历史回放数据")
public class EventReplayVO {

    @Schema(description = "事件基本信息")
    private EventInfo event;

    @Schema(description = "时间轴完整节点（可按时间拖动回放）")
    private List<TimelinePoint> timeline;

    @Schema(description = "每个时间节点的关联资产快照")
    private List<AssetSnapshot> assetSnapshots;

    @Schema(description = "处置总结")
    private EventSummary summary;

    @Data
    @Schema(description = "事件基本信息")
    public static class EventInfo {
        private Long id;
        private String eventCode;
        private String title;
        private Integer eventType;
        private String eventTypeName;
        private Integer eventLevel;
        private String eventLevelName;
        private String eventLevelColor;
        private BigDecimal lng;
        private BigDecimal lat;
        private BigDecimal affectRadius;
        private String areaName;
        private String description;
        private String discoverTime;
        private String firstResponseTime;
        private String startDisposalTime;
        private String finishDisposalTime;
        private String recoverTime;
        private Integer usedMinutes;
        private String usedMinutesText;
        private Integer status;
        private String statusName;
        private String currentStage;
        private Integer progress;
        private String responsiblePerson;
        private String commander;
        private Integer involvedDeptCount;
        private Integer estimatedAffectedUsers;
        private Integer actualAffectedUsers;
    }

    @Data
    @Schema(description = "时间轴节点")
    public static class TimelinePoint {
        private Long id;
        private Integer pointType;
        private String pointTypeName;
        private String title;
        private String description;
        private String occurTime;
        private Long relativeSeconds;
        private Integer durationMinutes;
        private Integer resourceType;
        private String resourceTypeName;
        private Long resourceId;
        private String resourceCode;
        private BigDecimal lng;
        private BigDecimal lat;
        private String operatorName;
        private String tags;
        private Integer importance;
        private String importanceName;
        private String statusText;
    }

    @Data
    @Schema(description = "资产快照（某时刻状态）")
    public static class AssetSnapshot {
        private String atTime;
        private List<AssetStatusItem> valves;
        private List<AssetStatusItem> alarms;
        private List<AssetStatusItem> excavations;
        private List<AssetStatusItem> workOrders;
    }

    @Data
    @Schema(description = "资产状态条目")
    public static class AssetStatusItem {
        private Long id;
        private String code;
        private String name;
        private Integer status;
        private String statusName;
        private BigDecimal lng;
        private BigDecimal lat;
        private String remark;
    }

    @Data
    @Schema(description = "处置总结")
    public static class EventSummary {
        private Integer totalResponseMinutes;
        private Integer totalDisposalMinutes;
        private Integer totalRecoverMinutes;
        private String goodPoints;
        private String improvementPoints;
        private Integer involvedPersonCount;
        private Integer involvedDeptCount;
        private Integer notificationsCount;
        private BigDecimal costEstimate;
    }
}
