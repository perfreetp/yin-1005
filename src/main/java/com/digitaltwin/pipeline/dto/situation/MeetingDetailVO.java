package com.digitaltwin.pipeline.dto.situation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "会议详情VO")
public class MeetingDetailVO {

    @Schema(description = "会议ID")
    private Long id;

    @Schema(description = "会议编号")
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

    @Schema(description = "会议类型名称")
    private String meetingTypeName;

    @Schema(description = "会议级别：1-普通 2-重要 3-紧急 4-特急")
    private Integer level;

    @Schema(description = "会议级别名称")
    private String levelName;

    @Schema(description = "会议级别颜色")
    private String levelColor;

    @Schema(description = "会议状态：1-待开始 2-进行中 3-已暂停 4-已结束 5-已归档")
    private Integer status;

    @Schema(description = "会议状态名称")
    private String statusName;

    @Schema(description = "会议状态颜色")
    private String statusColor;

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

    @Schema(description = "参会人列表")
    private List<AttendeeVO> attendees;

    @Schema(description = "决策列表")
    private List<DecisionVO> decisions;

    @Schema(description = "会议纪要")
    private SummaryVO summary;

    @Schema(description = "会议时间轴")
    private List<MeetingTimelineItem> timeline;

    @Data
    @Schema(description = "参会人VO")
    public static class AttendeeVO {
        @Schema(description = "ID")
        private Long id;

        @Schema(description = "会议ID")
        private Long meetingId;

        @Schema(description = "参会者类型：1-部门 2-个人 3-群组")
        private Integer attendeeType;

        @Schema(description = "参会者类型名称")
        private String attendeeTypeName;

        @Schema(description = "部门ID")
        private Long deptId;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户姓名")
        private String userName;

        @Schema(description = "用户电话")
        private String userPhone;

        @Schema(description = "角色：1-主持 2-参会 3-列席 4-记录")
        private Integer role;

        @Schema(description = "角色名称")
        private String roleName;

        @Schema(description = "加入状态：0-未响应 1-已确认 2-已加入 3-已离开 4-缺席")
        private Integer joinStatus;

        @Schema(description = "加入状态名称")
        private String joinStatusName;

        @Schema(description = "加入状态颜色")
        private String joinStatusColor;

        @Schema(description = "加入时间")
        private LocalDateTime joinTime;

        @Schema(description = "离开时间")
        private LocalDateTime leaveTime;

        @Schema(description = "首次响应时间")
        private LocalDateTime responseTime;

        @Schema(description = "是否关键决策人：0-否 1-是")
        private Integer isKeyDecisionMaker;

        @Schema(description = "备注")
        private String remark;
    }

    @Data
    @Schema(description = "决策VO")
    public static class DecisionVO {
        @Schema(description = "ID")
        private Long id;

        @Schema(description = "会议ID")
        private Long meetingId;

        @Schema(description = "决议编号（第X项）")
        private Integer decisionNo;

        @Schema(description = "决议标题")
        private String title;

        @Schema(description = "决议内容")
        private String content;

        @Schema(description = "决议详情")
        private String detail;

        @Schema(description = "决策类型：1-处置方案 2-资源调度 3-人员调配 4-下一步行动 5-其他")
        private Integer decisionType;

        @Schema(description = "决策类型名称")
        private String decisionTypeName;

        @Schema(description = "优先级：1-低 2-中 3-高 4-紧急")
        private Integer priority;

        @Schema(description = "优先级名称")
        private String priorityName;

        @Schema(description = "优先级颜色")
        private String priorityColor;

        @Schema(description = "状态：0-待执行 1-执行中 2-已完成 3-已否决")
        private Integer status;

        @Schema(description = "状态名称")
        private String statusName;

        @Schema(description = "状态颜色")
        private String statusColor;

        @Schema(description = "执行部门ID")
        private Long executorDeptId;

        @Schema(description = "执行部门名称")
        private String executorDeptName;

        @Schema(description = "执行人ID")
        private Long executorId;

        @Schema(description = "执行人姓名")
        private String executorName;

        @Schema(description = "截止时间")
        private LocalDateTime deadline;

        @Schema(description = "完成时间")
        private LocalDateTime finishTime;

        @Schema(description = "关联工单ID")
        private Long relatedWorkOrderId;

        @Schema(description = "关联任务ID")
        private Long relatedTaskId;

        @Schema(description = "创建人姓名")
        private String creatorName;

        @Schema(description = "备注")
        private String remark;

        @Schema(description = "实际落实结果/完成情况描述")
        private String actualResult;

        @Schema(description = "完成百分比 0-100")
        private Integer completionPercentage;

        @Schema(description = "是否超时：0否1是")
        private Integer isTimeout;

        @Schema(description = "最后更新时间")
        private LocalDateTime lastUpdateTime;

        @Schema(description = "最后更新人")
        private String lastUpdaterName;

        @Schema(description = "验证人")
        private String verificationPerson;

        @Schema(description = "验证时间")
        private LocalDateTime verificationTime;

        @Schema(description = "验证意见")
        private String verificationRemark;

        @Schema(description = "追踪记录列表")
        private List<DecisionTraceVO> traces;
    }

    @Data
    @Schema(description = "会议纪要VO")
    public static class SummaryVO {
        @Schema(description = "ID")
        private Long id;

        @Schema(description = "会议ID")
        private Long meetingId;

        @Schema(description = "会议编号")
        private String meetingCode;

        @Schema(description = "纪要标题")
        private String title;

        @Schema(description = "纪要内容")
        private String summaryContent;

        @Schema(description = "要点，JSON数组")
        private String keyPoints;

        @Schema(description = "决议摘要")
        private String decisions;

        @Schema(description = "参会人名单")
        private String attendeesList;

        @Schema(description = "缺席人及原因")
        private String absentees;

        @Schema(description = "后续跟进事项")
        private String followUpItems;

        @Schema(description = "生成时间")
        private LocalDateTime generatedTime;

        @Schema(description = "生成人姓名")
        private String generatorName;

        @Schema(description = "审核人姓名")
        private String approverName;

        @Schema(description = "状态：1-草稿 2-已审核 3-已发布")
        private Integer status;

        @Schema(description = "状态名称")
        private String statusName;

        @Schema(description = "版本号")
        private Integer version;

        @Schema(description = "编辑人")
        private String editorName;

        @Schema(description = "最后编辑时间")
        private LocalDateTime editTime;

        @Schema(description = "决议总数")
        private Integer decisionTotalCount;

        @Schema(description = "已完成决议数")
        private Integer decisionCompletedCount;

        @Schema(description = "进行中决议数")
        private Integer decisionInProgressCount;

        @Schema(description = "待执行决议数")
        private Integer decisionPendingCount;

        @Schema(description = "已否决决议数")
        private Integer decisionVetoedCount;

        @Schema(description = "整体完成率(%)")
        private Integer overallCompletionRate;

        @Schema(description = "附件ID列表，逗号分隔")
        private String attachFileIds;
    }

    @Data
    @Schema(description = "会议时间轴项")
    public static class MeetingTimelineItem {
        @Schema(description = "时间点")
        private LocalDateTime time;

        @Schema(description = "类型：1-开始 2-加入 3-发言 4-决策 5-结束")
        private Integer type;

        @Schema(description = "类型名称")
        private String typeName;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "描述")
        private String description;

        @Schema(description = "操作人")
        private String operatorName;

        @Schema(description = "重要性")
        private Integer importance;
    }

    @Data
    @Schema(description = "决议落地追踪记录VO")
    public static class DecisionTraceVO {
        @Schema(description = "ID")
        private Long id;

        @Schema(description = "决议ID")
        private Long decisionId;

        @Schema(description = "会议ID")
        private Long meetingId;

        @Schema(description = "追踪类型：1-进度更新 2-状态变更 3-备注补充 4-验证确认")
        private Integer traceType;

        @Schema(description = "追踪类型名称")
        private String traceTypeName;

        @Schema(description = "更新内容")
        private String content;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "操作时间")
        private LocalDateTime operationTime;

        @Schema(description = "变更前状态")
        private Integer beforeStatus;

        @Schema(description = "变更前状态名称")
        private String beforeStatusName;

        @Schema(description = "变更后状态")
        private Integer afterStatus;

        @Schema(description = "变更后状态名称")
        private String afterStatusName;

        @Schema(description = "变更前进度")
        private Integer beforeProgress;

        @Schema(description = "变更后进度")
        private Integer afterProgress;
    }
}
