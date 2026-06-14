package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meeting_decision")
@Schema(description = "会商决策/决议")
public class MeetingDecision extends BaseEntity {

    @Schema(description = "会议ID")
    private Long meetingId;

    @Schema(description = "会议编号")
    private String meetingCode;

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

    @Schema(description = "优先级：1-低 2-中 3-高 4-紧急")
    private Integer priority;

    @Schema(description = "状态：0-待执行 1-执行中 2-已完成 3-已否决")
    private Integer status;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

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
}
