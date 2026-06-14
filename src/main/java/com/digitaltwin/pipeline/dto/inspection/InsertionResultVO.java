package com.digitaltwin.pipeline.dto.inspection;

import com.digitaltwin.pipeline.entity.inspection.InsertionTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "插单结果VO")
public class InsertionResultVO {

    @Schema(description = "插单任务信息")
    private InsertionTask insertionTask;

    @Schema(description = "受影响班组列表")
    private List<AffectedTeamVO> affectedTeams;

    @Schema(description = "新增任务的开始时间")
    private String insertStartTime;

    @Schema(description = "新增任务的结束时间")
    private String insertEndTime;

    @Schema(description = "是否需要加班")
    private Boolean needOvertime;

    @Schema(description = "是否需要增援")
    private Boolean needReinforcement;

    @Schema(description = "整体影响评估")
    private String impactAssessment;

    @Schema(description = "建议方案列表")
    private List<SuggestionPlanVO> suggestionPlans;

    @Data
    @Schema(description = "受影响班组VO")
    public static class AffectedTeamVO {

        @Schema(description = "班组ID")
        private Long teamId;

        @Schema(description = "班组名称")
        private String teamName;

        @Schema(description = "原任务数量")
        private Integer originalTaskCount;

        @Schema(description = "新任务数量")
        private Integer newTaskCount;

        @Schema(description = "总工时变化(小时)")
        private BigDecimal totalHoursChange;

        @Schema(description = "被调整的任务列表")
        private List<AdjustedTaskVO> adjustedTasks;

        @Schema(description = "具体冲突项列表")
        private List<ConflictItemVO> conflictItems;

        @Schema(description = "加班时长（小时）")
        private BigDecimal overtimeHours;

        @Schema(description = "工作量变化百分比")
        private Integer workloadChangePercent;
    }

    @Data
    @Schema(description = "调整任务VO")
    public static class AdjustedTaskVO {

        @Schema(description = "任务ID")
        private Long taskId;

        @Schema(description = "任务编号")
        private String taskCode;

        @Schema(description = "任务标题")
        private String taskTitle;

        @Schema(description = "原时间")
        private String originalTime;

        @Schema(description = "新时间")
        private String newTime;

        @Schema(description = "调整类型：1-延期 2-提前 3-换人 4-换车 5-换班组 6-取消 7-优先级变更")
        private Integer adjustmentType;

        @Schema(description = "调整类型名称")
        private String adjustmentTypeName;

        @Schema(description = "调整原因")
        private String adjustmentReason;
    }

    @Data
    @Schema(description = "冲突项VO")
    public static class ConflictItemVO {

        @Schema(description = "冲突类型：1-人员冲突 2-车辆冲突 3-时段重叠 4-技能不匹配 5-超时风险 6-跨区域过远")
        private Integer conflictType;

        @Schema(description = "冲突等级：1-轻微 2-中等 3-严重")
        private Integer conflictLevel;

        @Schema(description = "冲突描述")
        private String description;

        @Schema(description = "受影响的任务ID")
        private Long affectedTaskId;

        @Schema(description = "受影响的任务名称")
        private String affectedTaskName;

        @Schema(description = "调整建议")
        private String suggestion;
    }

    @Data
    @Schema(description = "建议方案VO")
    public static class SuggestionPlanVO {

        @Schema(description = "方案编号：A/B/C")
        private String planCode;

        @Schema(description = "方案名称")
        private String planName;

        @Schema(description = "方案描述")
        private String description;

        @Schema(description = "预计工时变化(小时)")
        private BigDecimal estimatedHoursChange;

        @Schema(description = "是否推荐")
        private Boolean recommended;

        @Schema(description = "冲突等级")
        private Integer conflictLevel;

        @Schema(description = "冲突数量")
        private Integer conflictCount;

        @Schema(description = "预计成本增加")
        private BigDecimal estimatedCostIncrease;

        @Schema(description = "是否推荐方案")
        private Boolean isRecommended;

        @Schema(description = "风险说明")
        private String riskDescription;
    }
}
