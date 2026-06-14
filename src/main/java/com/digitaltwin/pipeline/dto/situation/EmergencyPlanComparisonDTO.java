package com.digitaltwin.pipeline.dto.situation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "应急处置方案对比结果")
public class EmergencyPlanComparisonDTO {

    @Schema(description = "告警ID")
    private Long alarmId;

    @Schema(description = "管线ID")
    private Long pipelineId;

    @Schema(description = "管线编号")
    private String pipelineCode;

    @Schema(description = "管线类型名称")
    private String pipelineTypeName;

    @Schema(description = "事故发生时间")
    private String incidentTime;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "对比方案列表")
    private List<DisposalPlanVO> plans;

    @Schema(description = "推荐方案ID")
    private Long recommendedPlanId;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Data
    @Schema(description = "处置方案")
    public static class DisposalPlanVO {

        @Schema(description = "方案ID")
        private Long planId;

        @Schema(description = "方案名称")
        private String planName;

        @Schema(description = "方案描述")
        private String description;

        @Schema(description = "策略类型：1-最小影响用户 2-最快恢复 3-最省资源 4-最安全")
        private Integer strategyType;

        @Schema(description = "阀门动作列表")
        private List<ValveAction> valveActions;

        @Schema(description = "影响区域")
        private AffectedAreaVO affected;

        @Schema(description = "恢复时间评估")
        private RecoveryVO recovery;

        @Schema(description = "资源需求")
        private ResourceVO resources;

        @Schema(description = "风险评估")
        private RiskVO risk;

        @Schema(description = "综合评分")
        private ScoreVO score;
    }

    @Data
    @Schema(description = "阀门动作")
    public static class ValveAction {

        @Schema(description = "顺序号")
        private Integer orderNo;

        @Schema(description = "阀门ID")
        private Long valveId;

        @Schema(description = "阀门编号")
        private String valveCode;

        @Schema(description = "阀门名称")
        private String valveName;

        @Schema(description = "动作：1-关阀 2-节流 0-不动")
        private Integer action;

        @Schema(description = "是否可远程控制：0-否 1-是")
        private Integer remoteControllable;

        @Schema(description = "操作耗时（分钟）")
        private Integer operationMinutes;
    }

    @Data
    @Schema(description = "影响区域")
    public static class AffectedAreaVO {

        @Schema(description = "影响等级：1-局部 2-片区 3-区域 4-大范围")
        private Integer impactLevel;

        @Schema(description = "影响等级名称")
        private String impactLevelName;

        @Schema(description = "影响半径（米）")
        private BigDecimal radius;

        @Schema(description = "影响用户数")
        private Integer affectedUsers;

        @Schema(description = "影响企业数")
        private Integer affectedEnterprises;

        @Schema(description = "影响道路列表")
        private List<String> affectedRoads;

        @Schema(description = "影响建筑列表")
        private List<String> affectedBuildings;

        @Schema(description = "需通知方列表")
        private List<String> needNotifyParties;
    }

    @Data
    @Schema(description = "恢复评估")
    public static class RecoveryVO {

        @Schema(description = "总隔离时间（分钟）")
        private Integer totalIsolationMinutes;

        @Schema(description = "总维修时间（分钟）")
        private Integer totalRepairMinutes;

        @Schema(description = "总恢复时间（分钟）")
        private Integer totalRecoveryMinutes;

        @Schema(description = "恢复时间线")
        private List<RecoveryStepVO> recoveryTimeline;
    }

    @Data
    @Schema(description = "恢复步骤")
    public static class RecoveryStepVO {

        @Schema(description = "时间偏移（分钟）")
        private Integer timeOffsetMinutes;

        @Schema(description = "步骤名称")
        private String stepName;

        @Schema(description = "步骤描述")
        private String description;
    }

    @Data
    @Schema(description = "资源需求")
    public static class ResourceVO {

        @Schema(description = "所需工人数量")
        private Integer requiredWorkers;

        @Schema(description = "所需车辆数量")
        private Integer requiredVehicles;

        @Schema(description = "所需材料列表")
        private List<String> requiredMaterials;
    }

    @Data
    @Schema(description = "风险评估")
    public static class RiskVO {

        @Schema(description = "安全风险：1-低 2-中 3-高 4-极高")
        private Integer safetyRisk;

        @Schema(description = "操作风险：1-低 2-中 3-高 4-极高")
        private Integer operationRisk;

        @Schema(description = "次生风险：1-低 2-中 3-高 4-极高")
        private Integer secondaryRisk;

        @Schema(description = "风险项列表")
        private List<String> riskItems;
    }

    @Data
    @Schema(description = "综合评分")
    public static class ScoreVO {

        @Schema(description = "总分（0-100）")
        private Integer overall;

        @Schema(description = "恢复速度评分（0-100）")
        private Integer recoveryTimeScore;

        @Schema(description = "用户影响评分（0-100）")
        private Integer userImpactScore;

        @Schema(description = "资源消耗评分（0-100）")
        private Integer resourceScore;

        @Schema(description = "安全评分（0-100）")
        private Integer safetyScore;

        @Schema(description = "优势列表")
        private List<String> pros;

        @Schema(description = "劣势列表")
        private List<String> cons;
    }
}
