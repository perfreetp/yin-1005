package com.digitaltwin.pipeline.dto.situation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "方案执行详情VO")
public class PlanExecutionDetailVO {

    @Schema(description = "执行记录ID")
    private Long id;

    @Schema(description = "执行记录编号")
    private String recordCode;

    @Schema(description = "告警ID")
    private Long alarmId;

    @Schema(description = "告警编号")
    private String alarmCode;

    @Schema(description = "事件ID")
    private Long eventId;

    @Schema(description = "事件编号")
    private String eventCode;

    @Schema(description = "方案ID")
    private Long planId;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "策略类型：1-最小影响用户 2-最快恢复 3-最省资源 4-最安全")
    private Integer strategyType;

    @Schema(description = "策略类型名称")
    private String strategyTypeName;

    @Schema(description = "执行状态：1-待执行 2-执行中 3-已完成 4-部分成功 5-执行失败")
    private Integer status;

    @Schema(description = "执行状态名称")
    private String statusName;

    @Schema(description = "执行人")
    private String executor;

    @Schema(description = "执行部门")
    private String executorDept;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "实际总耗时（分钟）")
    private Integer totalUsedMinutes;

    @Schema(description = "预估总耗时（分钟）")
    private Integer estimatedTotalMinutes;

    @Schema(description = "实际影响用户数")
    private Integer actualAffectedUsers;

    @Schema(description = "预估影响用户数")
    private Integer estimatedAffectedUsers;

    @Schema(description = "实际影响企业数")
    private Integer actualAffectedEnterprises;

    @Schema(description = "预估影响企业数")
    private Integer estimatedAffectedEnterprises;

    @Schema(description = "时间偏差（分钟），正慢负快")
    private Integer deviationMinutes;

    @Schema(description = "用户影响偏差")
    private Integer deviationUsers;

    @Schema(description = "准确度评分（0-100）")
    private Integer accuracyScore;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "阀门操作清单")
    private List<ValveOperationVO> valveOperations;

    @Schema(description = "执行时间轴")
    private List<TimelineItemVO> timeline;

    @Schema(description = "计划vs实际对比汇总")
    private ExecutionComparisonVO comparison;

    @Schema(description = "复盘分析")
    private ReplayAnalysisVO analysis;

    @Schema(description = "偏差说明文字")
    private String deviationDescription;

    @Data
    @Schema(description = "阀门操作VO")
    public static class ValveOperationVO {

        @Schema(description = "记录ID")
        private Long id;

        @Schema(description = "阀门ID")
        private Long valveId;

        @Schema(description = "阀门编号")
        private String valveCode;

        @Schema(description = "阀门名称")
        private String valveName;

        @Schema(description = "计划动作：1-关阀 2-节流 0-不动")
        private Integer plannedAction;

        @Schema(description = "计划动作名称")
        private String plannedActionName;

        @Schema(description = "实际动作：1-关阀 2-节流 0-不动")
        private Integer actualAction;

        @Schema(description = "实际动作名称")
        private String actualActionName;

        @Schema(description = "计划执行顺序号")
        private Integer plannedOrderNo;

        @Schema(description = "实际执行顺序号")
        private Integer actualOrderNo;

        @Schema(description = "计划操作耗时（分钟）")
        private Integer plannedMinutes;

        @Schema(description = "实际操作耗时（分钟）")
        private Integer actualMinutes;

        @Schema(description = "操作时间偏差（分钟）")
        private Integer timeDeviation;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "操作时间")
        private LocalDateTime operationTime;

        @Schema(description = "是否可远程控制：0-否 1-是")
        private Integer remoteControllable;

        @Schema(description = "实际是否远程操作：0-否 1-是")
        private Integer actualIsRemote;

        @Schema(description = "是否成功：0-否 1-是")
        private Integer isSuccessful;

        @Schema(description = "是否成功名称")
        private String isSuccessfulName;

        @Schema(description = "失败原因")
        private String failReason;

        @Schema(description = "操作前状态")
        private String beforeStatus;

        @Schema(description = "操作后状态")
        private String afterStatus;

        @Schema(description = "动作是否一致：0-不一致 1-一致")
        private Integer actionConsistent;

        @Schema(description = "备注")
        private String remark;
    }

    @Data
    @Schema(description = "时间轴节点VO")
    public static class TimelineItemVO {

        @Schema(description = "节点ID")
        private Long id;

        @Schema(description = "节点类型：1-方案选定 2-派单 3-到达现场 4-开始关阀 5-完成关阀 6-开始维修 7-完成维修 8-开始恢复 9-恢复完成 10-意外事件")
        private Integer pointType;

        @Schema(description = "节点类型名称")
        private String pointTypeName;

        @Schema(description = "节点标题")
        private String title;

        @Schema(description = "节点描述")
        private String description;

        @Schema(description = "发生时间")
        private LocalDateTime occurTime;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "是否关键节点：0-否 1-是")
        private Integer isKeyNode;

        @Schema(description = "关联资源类型")
        private String resourceType;

        @Schema(description = "关联资源ID")
        private Long resourceId;

        @Schema(description = "关联资源编号")
        private String resourceCode;
    }

    @Data
    @Schema(description = "执行对比汇总VO")
    public static class ExecutionComparisonVO {

        @Schema(description = "预估总耗时（分钟）")
        private Integer estimatedTotalMinutes;

        @Schema(description = "实际总耗时（分钟）")
        private Integer actualTotalMinutes;

        @Schema(description = "时间偏差（分钟）")
        private Integer timeDeviation;

        @Schema(description = "时间偏差率（%）")
        private Double timeDeviationRate;

        @Schema(description = "预估影响用户数")
        private Integer estimatedAffectedUsers;

        @Schema(description = "实际影响用户数")
        private Integer actualAffectedUsers;

        @Schema(description = "用户影响偏差")
        private Integer userDeviation;

        @Schema(description = "用户影响偏差率（%）")
        private Double userDeviationRate;

        @Schema(description = "预估影响企业数")
        private Integer estimatedAffectedEnterprises;

        @Schema(description = "实际影响企业数")
        private Integer actualAffectedEnterprises;

        @Schema(description = "企业影响偏差")
        private Integer enterpriseDeviation;

        @Schema(description = "计划阀门操作数")
        private Integer plannedValveCount;

        @Schema(description = "实际阀门操作数")
        private Integer actualValveCount;

        @Schema(description = "成功阀门操作数")
        private Integer successfulValveCount;

        @Schema(description = "失败阀门操作数")
        private Integer failedValveCount;

        @Schema(description = "阀门操作成功率（%）")
        private Double valveSuccessRate;

        @Schema(description = "阀门操作错误数")
        private Integer valveErrorCount;

        @Schema(description = "准确度评分（0-100）")
        private Integer accuracyScore;
    }

    @Data
    @Schema(description = "复盘分析VO")
    public static class ReplayAnalysisVO {

        @Schema(description = "分析ID")
        private Long id;

        @Schema(description = "方案准确度评分（0-100）")
        private Integer planAccuracyScore;

        @Schema(description = "时间偏差评分（0-100）")
        private Integer timeDeviationScore;

        @Schema(description = "影响偏差评分（0-100）")
        private Integer impactDeviationScore;

        @Schema(description = "操作准确度评分（0-100）")
        private Integer operationAccuracyScore;

        @Schema(description = "综合评分（0-100）")
        private Integer overallScore;

        @Schema(description = "做得好的方面")
        private String goodPoints;

        @Schema(description = "需改进的方面")
        private String improvementPoints;

        @Schema(description = "优化建议")
        private String suggestions;

        @Schema(description = "经验教训")
        private String learnedLessons;

        @Schema(description = "复盘人姓名")
        private String reviewerName;

        @Schema(description = "复盘时间")
        private LocalDateTime reviewTime;
    }
}
