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
    }
}
