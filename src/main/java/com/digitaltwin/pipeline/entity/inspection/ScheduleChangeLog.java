package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule_change_log")
@Schema(description = "排班变更日志")
public class ScheduleChangeLog extends BaseEntity {

    @Schema(description = "变更编号")
    private String changeCode;

    @Schema(description = "变更类型：1-临时插单 2-任务取消 3-人员调整 4-车辆调整 5-任务延期 6-区域变更 7-优先级变更")
    private Integer changeType;

    @Schema(description = "变更原因：1-突发告警 2-突发抢修 3-人员请假 4-车辆故障 5-上级安排 6-其他")
    private Integer changeReason;

    @Schema(description = "原因详情")
    private String reasonDetail;

    @Schema(description = "排班日期，yyyy-MM-dd")
    private String scheduleDate;

    @Schema(description = "受影响班组ID，逗号分隔")
    private String affectedTeamIds;

    @Schema(description = "受影响班组名，逗号分隔")
    private String affectedTeamNames;

    @Schema(description = "受影响任务数")
    private Integer affectedTaskCount;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作人部门")
    private String operatorDept;

    @Schema(description = "变更时间")
    private LocalDateTime changeTime;

    @Schema(description = "改前排班JSON快照")
    private String beforeSnapshot;

    @Schema(description = "改后排班JSON快照")
    private String afterSnapshot;

    @Schema(description = "状态：1-草稿 2-已确认 3-已执行 4-已回滚")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "冲突等级")
    private Integer conflictLevel;

    @Schema(description = "是否回滚操作：0否1是")
    private Integer isRollback;

    @Schema(description = "回滚对应的原日志ID")
    private Long rollbackFromLogId;

    @Schema(description = "同步目标：calendar/im/email 等，JSON数组")
    private String syncTargets;

    @Schema(description = "可回滚有效期（分钟）")
    private Integer rollbackValidMinutes;

    @Schema(description = "操作人电话")
    private String operatorPhone;
}
