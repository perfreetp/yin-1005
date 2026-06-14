package com.digitaltwin.pipeline.dto.situation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "当前态势快照（一张图首页用）")
public class SituationSnapshotVO {

    @Schema(description = "统计时间")
    private String snapshotTime;

    @Schema(description = "核心指标汇总")
    private SituationMetrics metrics;

    @Schema(description = "实时告警列表（按级别）")
    private List<SituationEventItem> activeAlarms;

    @Schema(description = "施工中开挖列表")
    private List<SituationEventItem> activeExcavations;

    @Schema(description = "处置中事件列表")
    private List<SituationEventItem> activeIncidents;

    @Schema(description = "在办工单列表（按紧急度）")
    private List<SituationEventItem> activeWorkOrders;

    @Schema(description = "异常阀门列表")
    private List<SituationAssetItem> abnormalValves;

    @Schema(description = "按区域统计（热力图用）")
    private List<SituationAreaStat> areaStats;

    @Schema(description = "按管线类型统计")
    private List<SituationPipelineTypeStat> pipelineTypeStats;

    @Schema(description = "未来24小时预测")
    private SituationForecast forecast;

    @Data
    @Schema(description = "态势核心指标")
    public static class SituationMetrics {
        @Schema(description = "待响应告警数")
        private Integer pendingAlarmCount;
        @Schema(description = "处置中事件数")
        private Integer handlingIncidentCount;
        @Schema(description = "施工中开挖数")
        private Integer constructingExcavationCount;
        @Schema(description = "在办工单数")
        private Integer activeWorkOrderCount;
        @Schema(description = "离线传感器数")
        private Integer offlineSensorCount;
        @Schema(description = "待巡检点数")
        private Integer pendingInspectionCount;
        @Schema(description = "高风险隐患点数量")
        private Integer highRiskHazardCount;
        @Schema(description = "整体态势评分：0-100，越高越稳定")
        private Integer overallHealthScore;
        @Schema(description = "态势级别：1-平稳 2-关注 3-预警 4-紧急")
        private Integer overallLevel;
        @Schema(description = "态势级别名称")
        private String overallLevelName;
        @Schema(description = "较昨日变化趋势：-2大幅恶化 -1略有恶化 0持平 1略有好转 2明显好转")
        private Integer trend;
    }

    @Data
    @Schema(description = "事件条目")
    public static class SituationEventItem {
        private Long id;
        private String code;
        private String title;
        private Integer type;
        private String typeName;
        private Integer level;
        private String levelName;
        private String levelColor;
        private BigDecimal lng;
        private BigDecimal lat;
        private String areaName;
        private String currentStage;
        private Integer progress;
        private String durationText;
        private String updateTime;
    }

    @Data
    @Schema(description = "资产异常条目")
    public static class SituationAssetItem {
        private Long id;
        private String code;
        private String name;
        private String assetType;
        private Integer abnormalType;
        private String abnormalName;
        private BigDecimal lng;
        private BigDecimal lat;
        private String updateTime;
    }

    @Data
    @Schema(description = "区域态势统计")
    public static class SituationAreaStat {
        private String areaCode;
        private String areaName;
        private Integer alarmCount;
        private Integer incidentCount;
        private Integer workOrderCount;
        private Integer level;
        private String levelName;
        private BigDecimal centerLng;
        private BigDecimal centerLat;
    }

    @Data
    @Schema(description = "管线类型统计")
    public static class SituationPipelineTypeStat {
        private Integer pipelineType;
        private String pipelineTypeName;
        private Integer assetCount;
        private Integer alarmCount;
        private Integer incidentCount;
        private BigDecimal healthScore;
    }

    @Data
    @Schema(description = "未来预测")
    public static class SituationForecast {
        @Schema(description = "预测时间点，未来24小时，每小时1个值")
        private List<String> timeLabels;
        @Schema(description = "预计告警数曲线")
        private List<Integer> predictedAlarmCounts;
        @Schema(description = "预计工单数曲线")
        private List<Integer> predictedWorkOrderCounts;
        @Schema(description = "高风险时段标记")
        private List<Integer> highRiskHourIndices;
        @Schema(description = "风险提示")
        private String riskTip;
    }
}
