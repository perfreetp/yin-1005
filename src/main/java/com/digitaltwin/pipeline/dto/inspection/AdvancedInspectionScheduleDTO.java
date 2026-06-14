package com.digitaltwin.pipeline.dto.inspection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "增强巡检排程结果")
public class AdvancedInspectionScheduleDTO extends SmartInspectionScheduleDTO {

    @Schema(description = "每个班组的日程安排")
    private List<TeamDailySchedule> teamSchedules;

    @Schema(description = "甘特图数据")
    private ResourceGanttChartVO gantt;

    @Schema(description = "资源使用率")
    private ResourceUsageVO resourceUsage;

    @Schema(description = "排程冲突列表")
    private List<SchedulingConflictVO> conflicts;

    @Data
    @Schema(description = "班组日程")
    public static class TeamDailySchedule {

        @Schema(description = "班组ID")
        private Long teamId;

        @Schema(description = "班组编码")
        private String teamCode;

        @Schema(description = "班组名称")
        private String teamName;

        @Schema(description = "班长姓名")
        private String leaderName;

        @Schema(description = "联系电话")
        private String contactPhone;

        @Schema(description = "成员列表")
        private List<InspectorVO> members;

        @Schema(description = "分配车辆")
        private VehicleVO assignedVehicle;

        @Schema(description = "今日总工时(小时)")
        private BigDecimal totalHours;

        @Schema(description = "今日总里程(km)")
        private BigDecimal totalKm;

        @Schema(description = "任务数量")
        private Integer taskCount;

        @Schema(description = "30分钟粒度日程块")
        private List<TimeSlotTask> timeSlots;
    }

    @Data
    @Schema(description = "巡检人员VO")
    public static class InspectorVO {

        @Schema(description = "人员ID")
        private Long id;

        @Schema(description = "人员编号")
        private String inspectorCode;

        @Schema(description = "姓名")
        private String name;

        @Schema(description = "联系电话")
        private String phone;

        @Schema(description = "技能，逗号分隔")
        private String skills;

        @Schema(description = "工作年限")
        private Integer experienceYears;

        @Schema(description = "工作状态：1-空闲 2-在途 3-作业中")
        private Integer workStatus;
    }

    @Data
    @Schema(description = "车辆VO")
    public static class VehicleVO {

        @Schema(description = "车辆ID")
        private Long id;

        @Schema(description = "车牌号")
        private String plateNumber;

        @Schema(description = "车辆类型：1-巡检车 2-抢修车 3-工程车")
        private Integer vehicleType;

        @Schema(description = "车辆类型名称")
        private String vehicleTypeName;

        @Schema(description = "载重(kg)")
        private Integer loadCapacity;
    }

    @Data
    @Schema(description = "时间段任务")
    public static class TimeSlotTask {

        @Schema(description = "时段序号（从0开始）")
        private Integer slotIndex;

        @Schema(description = "开始时间 HH:mm")
        private String startTime;

        @Schema(description = "结束时间 HH:mm")
        private String endTime;

        @Schema(description = "任务类型：1-巡检 2-行驶 3-休息 4-工单处置")
        private Integer taskType;

        @Schema(description = "任务类型名称")
        private String taskTypeName;

        @Schema(description = "任务标题")
        private String taskTitle;

        @Schema(description = "位置名称")
        private String locationName;

        @Schema(description = "经度")
        private BigDecimal lng;

        @Schema(description = "纬度")
        private BigDecimal lat;

        @Schema(description = "持续时长(分钟)")
        private Integer durationMinutes;

        @Schema(description = "优先级")
        private Integer priority;
    }

    @Data
    @Schema(description = "甘特图数据")
    public static class ResourceGanttChartVO {

        @Schema(description = "时间轴起始 HH:mm")
        private String timelineStart;

        @Schema(description = "时间轴结束 HH:mm")
        private String timelineEnd;

        @Schema(description = "班组甘特条目")
        private List<TeamGanttEntry> teamEntries;

        @Data
        @Schema(description = "班组甘特条目")
        public static class TeamGanttEntry {

            @Schema(description = "班组ID")
            private Long teamId;

            @Schema(description = "班组名称")
            private String teamName;

            @Schema(description = "任务时间块")
            private List<GanttTimeBlock> blocks;
        }

        @Data
        @Schema(description = "甘特时间块")
        public static class GanttTimeBlock {

            @Schema(description = "起始HH:mm")
            private String start;

            @Schema(description = "结束HH:mm")
            private String end;

            @Schema(description = "任务类型：1-巡检 2-行驶 3-休息 4-工单处置")
            private Integer taskType;

            @Schema(description = "任务类型名称")
            private String taskTypeName;

            @Schema(description = "任务标题")
            private String taskTitle;
        }
    }

    @Data
    @Schema(description = "资源使用率")
    public static class ResourceUsageVO {

        @Schema(description = "人员总数")
        private Integer totalInspectors;

        @Schema(description = "已排班人员数")
        private Integer assignedInspectors;

        @Schema(description = "人员使用率(%)")
        private BigDecimal inspectorUsageRate;

        @Schema(description = "车辆总数")
        private Integer totalVehicles;

        @Schema(description = "已排班车辆数")
        private Integer assignedVehicles;

        @Schema(description = "车辆使用率(%)")
        private BigDecimal vehicleUsageRate;

        @Schema(description = "是否有加班预警")
        private Boolean hasOvertimeWarning;

        @Schema(description = "加班预警班组列表")
        private List<String> overtimeTeamNames;
    }

    @Data
    @Schema(description = "排程冲突")
    public static class SchedulingConflictVO {

        @Schema(description = "冲突类型：1-能力不匹配 2-超工时 3-车辆不足 4-跨区域 5-技能不足")
        private Integer conflictType;

        @Schema(description = "冲突类型名称")
        private String conflictTypeName;

        @Schema(description = "严重级别：1-提示 2-警告 3-严重")
        private Integer severity;

        @Schema(description = "严重级别名称")
        private String severityName;

        @Schema(description = "关联班组ID")
        private Long teamId;

        @Schema(description = "关联班组名称")
        private String teamName;

        @Schema(description = "冲突描述")
        private String description;

        @Schema(description = "建议方案")
        private String suggestion;
    }
}
