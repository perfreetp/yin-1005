package com.digitaltwin.pipeline.dto.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "告警处置建议结果")
public class AlarmDisposalSuggestionDTO {

    @Schema(description = "告警ID")
    private Long alarmId;

    @Schema(description = "告警编号")
    private String alarmCode;

    @Schema(description = "告警标题")
    private String title;

    @Schema(description = "告警类型")
    private Integer alarmType;

    @Schema(description = "告警类型名称")
    private String alarmTypeName;

    @Schema(description = "原始告警级别")
    private Integer originalLevel;

    @Schema(description = "综合处置优先级：1-常规 2-关注 3-紧急 4-特急")
    private Integer disposalPriority;

    @Schema(description = "处置优先级名称")
    private String disposalPriorityName;

    @Schema(description = "建议响应时长（分钟）")
    private Integer suggestedResponseMinutes;

    @Schema(description = "建议到达现场时长（分钟）")
    private Integer suggestedArrivalMinutes;

    @Schema(description = "告警趋势：1-正在恶化 2-持续稳定 3-正在缓解 4-已恢复")
    private Integer trendStatus;

    @Schema(description = "告警趋势名称")
    private String trendStatusName;

    @Schema(description = "最近1小时读数（用于值班人员判断趋势）")
    private List<ReadingPoint> recentReadings;

    @Schema(description = "建议处置流程步骤")
    private List<DisposalStep> disposalSteps;

    @Schema(description = "建议关阀顺序列表（按优先级）")
    private List<ValveClosingOrder> suggestedValveClosures;

    @Schema(description = "预计影响区域")
    private AffectedAreaDTO affectedArea;

    @Schema(description = "关联隐患点信息")
    private RelatedHazardInfo relatedHazard;

    @Schema(description = "关联未完成工单信息")
    private RelatedWorkOrderInfo relatedWorkOrder;

    @Schema(description = "历史同类告警统计（30天内）")
    private HistoryAlarmStats historyStats;

    @Schema(description = "值班处置提醒")
    private String operatorReminder;

    @Schema(description = "推荐承办部门")
    private String suggestedDepartment;

    @Schema(description = "推荐承办人")
    private String suggestedUndertaker;

    @Schema(description = "推荐联系电话")
    private String suggestedPhone;

    @Data
    @Schema(description = "读数趋势点")
    public static class ReadingPoint {
        @Schema(description = "时间")
        private String time;
        @Schema(description = "读数")
        private BigDecimal value;
        @Schema(description = "是否超出阈值")
        private Boolean outOfRange;
    }

    @Data
    @Schema(description = "处置步骤")
    public static class DisposalStep {
        @Schema(description = "步骤序号")
        private Integer stepNo;
        @Schema(description = "步骤名称")
        private String stepName;
        @Schema(description = "操作说明")
        private String instruction;
        @Schema(description = "是否必须：0-否 1-是")
        private Integer required;
        @Schema(description = "预计耗时（分钟）")
        private Integer estimatedMinutes;
        @Schema(description = "注意事项")
        private String caution;
    }

    @Data
    @Schema(description = "关阀顺序")
    public static class ValveClosingOrder {
        @Schema(description = "顺序号")
        private Integer orderNo;
        @Schema(description = "阀门ID")
        private Long valveId;
        @Schema(description = "阀门编号")
        private String valveCode;
        @Schema(description = "阀门名称")
        private String valveName;
        @Schema(description = "阀门类型")
        private String valveType;
        @Schema(description = "口径(mm)")
        private BigDecimal diameter;
        @Schema(description = "经度")
        private BigDecimal lng;
        @Schema(description = "纬度")
        private BigDecimal lat;
        @Schema(description = "距离告警点距离(m)")
        private BigDecimal distance;
        @Schema(description = "关阀说明")
        private String description;
        @Schema(description = "是否远程可控：0-否 1-是")
        private Integer remoteControllable;
        @Schema(description = "预计影响用户数")
        private Integer estimatedAffectedUsers;
    }

    @Data
    @Schema(description = "预计影响区域")
    public static class AffectedAreaDTO {
        @Schema(description = "影响等级：1-局部 2-片区 3-区域 4-大范围")
        private Integer impactLevel;
        @Schema(description = "影响等级名称")
        private String impactLevelName;
        @Schema(description = "中心点经度")
        private BigDecimal centerLng;
        @Schema(description = "中心点纬度")
        private BigDecimal centerLat;
        @Schema(description = "影响半径(m)")
        private BigDecimal radius;
        @Schema(description = "影响边界（WKT格式）")
        private String boundary;
        @Schema(description = "影响道路列表")
        private List<String> affectedRoads;
        @Schema(description = "影响小区/建筑列表")
        private List<String> affectedBuildings;
        @Schema(description = "预计影响用户数")
        private Integer estimatedAffectedUsers;
        @Schema(description = "预计影响企业数")
        private Integer estimatedAffectedEnterprises;
        @Schema(description = "需通知的社区/物业")
        private List<String> needNotifyParties;
    }

    @Data
    @Schema(description = "关联隐患点信息")
    public static class RelatedHazardInfo {
        @Schema(description = "是否存在关联隐患：0-否 1-是")
        private Integer exists;
        @Schema(description = "隐患点ID")
        private Long hazardId;
        @Schema(description = "隐患编号")
        private String hazardCode;
        @Schema(description = "风险等级")
        private Integer riskLevel;
        @Schema(description = "风险评分")
        private Integer riskScore;
        @Schema(description = "隐患状态")
        private Integer status;
        @Schema(description = "发现至今天数")
        private Integer daysSinceDiscovered;
    }

    @Data
    @Schema(description = "关联未完成工单")
    public static class RelatedWorkOrderInfo {
        @Schema(description = "是否存在未完成工单：0-否 1-是")
        private Integer exists;
        @Schema(description = "工单ID")
        private Long workOrderId;
        @Schema(description = "工单编号")
        private String orderCode;
        @Schema(description = "工单紧急程度")
        private Integer urgency;
        @Schema(description = "工单状态")
        private Integer status;
        @Schema(description = "当前处理节点")
        private String currentNode;
        @Schema(description = "已创建时长(小时)")
        private BigDecimal hoursSinceCreated;
    }

    @Data
    @Schema(description = "历史告警统计")
    public static class HistoryAlarmStats {
        @Schema(description = "30天内同类告警次数")
        private Integer last30DaysCount;
        @Schema(description = "7天内同类告警次数")
        private Integer last7DaysCount;
        @Schema(description = "24小时内同类告警次数")
        private Integer last24HoursCount;
        @Schema(description = "重复发生率")
        private BigDecimal recurrenceRate;
        @Schema(description = "是否为高频告警点：0-否 1-是")
        private Integer isFrequentPoint;
        @Schema(description = "平均处置时长(分钟)")
        private BigDecimal avgDisposalMinutes;
    }
}
