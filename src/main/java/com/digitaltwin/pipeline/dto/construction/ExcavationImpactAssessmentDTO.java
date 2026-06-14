package com.digitaltwin.pipeline.dto.construction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "开挖影响范围综合评估结果")
public class ExcavationImpactAssessmentDTO {

    @Schema(description = "综合影响等级：1-轻微 2-一般 3-较大 4-重大")
    private Integer impactLevel;

    @Schema(description = "综合影响等级名称")
    private String impactLevelName;

    @Schema(description = "影响评分(0-100)")
    private Integer impactScore;

    @Schema(description = "是否通过校核")
    private Boolean passed;

    @Schema(description = "受影响范围边界坐标（WKT格式）")
    private String impactBoundary;

    @Schema(description = "影响面积估算(㎡)")
    private BigDecimal estimatedImpactArea;

    @Schema(description = "受影响管线详情列表")
    private List<AffectedPipelineInfo> affectedPipelines;

    @Schema(description = "受影响阀门详情列表")
    private List<AffectedValveInfo> affectedValves;

    @Schema(description = "受影响井盖详情列表")
    private List<AffectedManholeInfo> affectedManholes;

    @Schema(description = "受影响道路信息")
    private List<AffectedRoadInfo> affectedRoads;

    @Schema(description = "建议通知部门列表")
    private List<NotificationDept> suggestedNotifications;

    @Schema(description = "管线冲突列表")
    private List<com.digitaltwin.pipeline.entity.construction.PipelineConflict> conflicts;

    @Schema(description = "应急关阀建议")
    private List<com.digitaltwin.pipeline.entity.construction.EmergencyValveSuggestion> valveSuggestions;

    @Schema(description = "统计摘要")
    private ImpactSummary summary;

    @Schema(description = "综合评估意见")
    private String reviewOpinion;

    @Data
    @Schema(description = "受影响管线信息")
    public static class AffectedPipelineInfo {
        @Schema(description = "管线ID")
        private Long pipelineId;
        @Schema(description = "管线编号")
        private String pipelineCode;
        @Schema(description = "管线名称")
        private String pipelineName;
        @Schema(description = "管线类型")
        private Integer pipelineType;
        @Schema(description = "管线类型名称")
        private String pipelineTypeName;
        @Schema(description = "管径(mm)")
        private BigDecimal diameter;
        @Schema(description = "管长(m)")
        private BigDecimal length;
        @Schema(description = "埋设深度(m)")
        private BigDecimal buriedDepth;
        @Schema(description = "材质")
        private Integer material;
        @Schema(description = "材质名称")
        private String materialName;
        @Schema(description = "产权单位")
        private String ownerUnit;
        @Schema(description = "运维单位")
        private String maintenanceUnit;
        @Schema(description = "影响等级：1-间接影响 2-邻近影响 3-直接影响 4-交叉穿越")
        private Integer influenceLevel;
        @Schema(description = "最小净距(m)")
        private BigDecimal minDistance;
        @Schema(description = "风险说明")
        private String riskDescription;
        @Schema(description = "保护建议")
        private String protectionSuggestion;
    }

    @Data
    @Schema(description = "受影响阀门信息")
    public static class AffectedValveInfo {
        @Schema(description = "阀门ID")
        private Long valveId;
        @Schema(description = "阀门编号")
        private String valveCode;
        @Schema(description = "阀门名称")
        private String valveName;
        @Schema(description = "阀门类型")
        private Integer valveType;
        @Schema(description = "阀门类型名称")
        private String valveTypeName;
        @Schema(description = "口径(mm)")
        private BigDecimal diameter;
        @Schema(description = "所属管线ID")
        private Long pipelineId;
        @Schema(description = "所属管线编号")
        private String pipelineCode;
        @Schema(description = "安装位置")
        private String location;
        @Schema(description = "距离开挖区距离(m)")
        private BigDecimal distance;
        @Schema(description = "是否需要关阀作业：0-否 1-是")
        private Integer needClosure;
        @Schema(description = "产权单位")
        private String ownerUnit;
    }

    @Data
    @Schema(description = "受影响井盖信息")
    public static class AffectedManholeInfo {
        @Schema(description = "井盖ID")
        private Long manholeId;
        @Schema(description = "井盖编号")
        private String manholeCode;
        @Schema(description = "井盖名称")
        private String manholeName;
        @Schema(description = "井盖类型")
        private Integer manholeType;
        @Schema(description = "井盖类型名称")
        private String manholeTypeName;
        @Schema(description = "所属管线ID")
        private Long pipelineId;
        @Schema(description = "所在道路")
        private String roadName;
        @Schema(description = "距离开挖区距离(m)")
        private BigDecimal distance;
        @Schema(description = "是否需要防护：0-否 1-是")
        private Integer needProtection;
        @Schema(description = "产权单位")
        private String ownerUnit;
    }

    @Data
    @Schema(description = "受影响道路信息")
    public static class AffectedRoadInfo {
        @Schema(description = "道路名称")
        private String roadName;
        @Schema(description = "道路等级：1-主干道 2-次干道 3-支路 4-小区道路")
        private Integer roadLevel;
        @Schema(description = "影响长度(m)")
        private BigDecimal affectedLength;
        @Schema(description = "占用车道数")
        private Integer occupiedLanes;
        @Schema(description = "交通影响：1-无影响 2-轻微拥堵 3-需改道 4-全封闭")
        private Integer trafficImpact;
        @Schema(description = "交通建议")
        private String trafficSuggestion;
        @Schema(description = "涉及管线类型数量")
        private Integer pipelineTypeCount;
        @Schema(description = "涉及管线类型名称列表")
        private List<String> pipelineTypeNames;
    }

    @Data
    @Schema(description = "建议通知部门")
    public static class NotificationDept {
        @Schema(description = "部门ID")
        private Long deptId;
        @Schema(description = "部门名称")
        private String deptName;
        @Schema(description = "通知原因")
        private String reason;
        @Schema(description = "紧急程度：1-提前告知 2-施工前通知 3-需现场交底 4-立即通知")
        private Integer urgency;
        @Schema(description = "负责人")
        private String leader;
        @Schema(description = "联系电话")
        private String phone;
        @Schema(description = "负责管线类型（逗号分隔）")
        private String pipelineTypes;
    }

    @Data
    @Schema(description = "统计摘要")
    public static class ImpactSummary {
        @Schema(description = "涉及管线总数")
        private Integer totalPipelines;
        @Schema(description = "涉及管线类型数")
        private Integer pipelineTypeCount;
        @Schema(description = "高风险管线数")
        private Integer highRiskPipelineCount;
        @Schema(description = "燃气管线数")
        private Integer gasPipelineCount;
        @Schema(description = "电力管线数")
        private Integer powerPipelineCount;
        @Schema(description = "给排水管线数")
        private Integer waterPipelineCount;
        @Schema(description = "阀门总数")
        private Integer totalValves;
        @Schema(description = "需关阀数量")
        private Integer needClosureValveCount;
        @Schema(description = "井盖总数")
        private Integer totalManholes;
        @Schema(description = "需防护井盖数")
        private Integer needProtectionManholeCount;
        @Schema(description = "影响道路数")
        private Integer totalRoads;
        @Schema(description = "主干道数量")
        private Integer mainRoadCount;
        @Schema(description = "需通知部门数")
        private Integer totalDepartments;
        @Schema(description = "需立即通知部门数")
        private Integer urgentDepartmentCount;
        @Schema(description = "预计影响用户数")
        private Integer estimatedAffectedUsers;
    }
}
