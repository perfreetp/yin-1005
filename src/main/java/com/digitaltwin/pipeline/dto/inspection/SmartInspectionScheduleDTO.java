package com.digitaltwin.pipeline.dto.inspection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "智能巡检调度结果")
public class SmartInspectionScheduleDTO {

    @Schema(description = "调度日期")
    private LocalDate scheduleDate;

    @Schema(description = "调度生成时间")
    private LocalDateTime generateTime;

    @Schema(description = "今日建议巡检总点数")
    private Integer totalPoints;

    @Schema(description = "今日建议巡检公里数")
    private BigDecimal totalDistanceKm;

    @Schema(description = "预计总工时（小时）")
    private BigDecimal estimatedTotalHours;

    @Schema(description = "建议巡检组数")
    private Integer suggestedTeamCount;

    @Schema(description = "待巡检点按区域统计")
    private List<AreaStatistics> areaStatistics;

    @Schema(description = "风险等级分布")
    private RiskDistribution riskDistribution;

    @Schema(description = "任务分组清单（按巡检组划分）")
    private List<TeamTaskGroup> teamTaskGroups;

    @Schema(description = "未安排、建议次日处理的低优先级点")
    private List<PendingInspectionPoint> postponedPoints;

    @Data
    @Schema(description = "区域统计")
    public static class AreaStatistics {
        @Schema(description = "区域编码")
        private String areaCode;
        @Schema(description = "区域名称")
        private String areaName;
        @Schema(description = "巡检点数")
        private Integer pointCount;
        @Schema(description = "距离(km)")
        private BigDecimal distanceKm;
    }

    @Data
    @Schema(description = "风险分布")
    public static class RiskDistribution {
        @Schema(description = "特高风险数量")
        private Integer criticalCount;
        @Schema(description = "高风险数量")
        private Integer highCount;
        @Schema(description = "中风险数量")
        private Integer mediumCount;
        @Schema(description = "低风险数量")
        private Integer lowCount;
        @Schema(description = "紧急工单数")
        private Integer urgentWorkOrders;
    }

    @Data
    @Schema(description = "巡检组任务包")
    public static class TeamTaskGroup {
        @Schema(description = "组号")
        private Integer teamNo;
        @Schema(description = "建议承担区域")
        private String assignedArea;
        @Schema(description = "任务数量")
        private Integer taskCount;
        @Schema(description = "总里程(km)")
        private BigDecimal totalDistanceKm;
        @Schema(description = "预计工时(小时)")
        private BigDecimal estimatedHours;
        @Schema(description = "建议出发时间")
        private String suggestedStartTime;
        @Schema(description = "建议完成时间")
        private String suggestedEndTime;
        @Schema(description = "任务路线（按巡检顺序排列）")
        private List<ScheduledInspectionTask> orderedTasks;
        @Schema(description = "本组最高风险")
        private Integer maxRisk;
    }

    @Data
    @Schema(description = "已排期巡检任务")
    public static class ScheduledInspectionTask {
        @Schema(description = "序号（执行顺序）")
        private Integer orderNo;
        @Schema(description = "待巡检点ID")
        private Long pointId;
        @Schema(description = "资产类型：1-管线 2-阀门 3-井盖 4-隐患点 5-工单")
        private Integer assetType;
        @Schema(description = "资产类型名称")
        private String assetTypeName;
        @Schema(description = "关联业务ID")
        private Long relatedId;
        @Schema(description = "关联业务编号")
        private String relatedCode;
        @Schema(description = "点名称/位置描述")
        private String locationName;
        @Schema(description = "经度")
        private BigDecimal lng;
        @Schema(description = "纬度")
        private BigDecimal lat;
        @Schema(description = "所属区域")
        private String areaCode;
        @Schema(description = "优先级得分：0-100")
        private Integer priorityScore;
        @Schema(description = "风险等级：1-低 2-中 3-高 4-特高")
        private Integer riskLevel;
        @Schema(description = "风险等级名称")
        private String riskLevelName;
        @Schema(description = "工单紧急度：1-普通 2-较急 3-急 4-特急")
        private Integer urgency;
        @Schema(description = "距上一节点距离(m)")
        private BigDecimal distanceFromPrev;
        @Schema(description = "建议巡检要点")
        private String inspectionFocus;
        @Schema(description = "建议到达时间")
        private String estimatedArrival;
    }

    @Data
    @Schema(description = "待排期待巡检点")
    public static class PendingInspectionPoint {
        @Schema(description = "待巡检点ID")
        private Long pointId;
        @Schema(description = "资产类型")
        private Integer assetType;
        @Schema(description = "关联ID")
        private Long relatedId;
        @Schema(description = "位置")
        private String locationName;
        @Schema(description = "优先级得分")
        private Integer priorityScore;
        @Schema(description = "建议顺延天数")
        private Integer suggestedPostponeDays;
        @Schema(description = "顺延原因")
        private String postponeReason;
    }
}
