package com.digitaltwin.pipeline.dto.inspection;

import com.digitaltwin.pipeline.entity.inspection.ScheduleChangeLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "当日日程视图VO")
public class DayScheduleVO {

    @Schema(description = "排班日期")
    private String scheduleDate;

    @Schema(description = "班组日程列表")
    private List<AdvancedInspectionScheduleDTO.TeamDailySchedule> teamSchedules;

    @Schema(description = "变更历史")
    private List<ScheduleChangeLog> changeLogs;

    @Schema(description = "总任务数")
    private Integer totalTaskCount;

    @Schema(description = "已完成任务数")
    private Integer completedCount;

    @Schema(description = "进行中任务数")
    private Integer inProgressCount;

    @Schema(description = "整体利用率%")
    private Integer overallUtilizationRate;
}
