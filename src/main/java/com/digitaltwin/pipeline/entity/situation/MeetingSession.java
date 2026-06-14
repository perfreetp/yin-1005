package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meeting_session")
@Schema(description = "会商会议")
public class MeetingSession extends BaseEntity {

    @Schema(description = "会议编号，格式 MS-yyyyMMdd-xxxx")
    private String meetingCode;

    @Schema(description = "会议标题")
    private String title;

    @Schema(description = "会议描述")
    private String description;

    @Schema(description = "关联事件ID")
    private Long eventId;

    @Schema(description = "关联事件编号")
    private String eventCode;

    @Schema(description = "触发来源类型")
    private Integer sourceType;

    @Schema(description = "触发来源ID")
    private Long sourceId;

    @Schema(description = "会议类型：1-紧急会商 2-例行会商 3-专题会商 4-复盘会")
    private Integer meetingType;

    @Schema(description = "会议级别：1-普通 2-重要 3-紧急 4-特急")
    private Integer level;

    @Schema(description = "会议状态：1-待开始 2-进行中 3-已暂停 4-已结束 5-已归档")
    private Integer status;

    @Schema(description = "发起人ID")
    private Long initiatorId;

    @Schema(description = "发起人姓名")
    private String initiatorName;

    @Schema(description = "发起人部门")
    private String initiatorDept;

    @Schema(description = "计划开始时间")
    private LocalDateTime startTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime endTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "会议时长（分钟）")
    private Integer durationMinutes;

    @Schema(description = "参会部门ID列表，逗号分隔")
    private String deptIds;

    @Schema(description = "参会部门名称列表，逗号分隔")
    private String deptNames;

    @Schema(description = "应到人数")
    private Integer attendeeCount;

    @Schema(description = "实到人数")
    private Integer actualAttendeeCount;

    @Schema(description = "决策数量")
    private Integer decisionCount;

    @Schema(description = "会议记录正文")
    private String recordContent;

    @Schema(description = "最终结论")
    private String conclusion;

    @Schema(description = "待办事项，JSON数组")
    private String followUpTasks;

    @Schema(description = "备注")
    private String remark;
}
