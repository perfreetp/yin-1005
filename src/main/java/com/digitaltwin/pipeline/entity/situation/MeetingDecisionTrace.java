package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meeting_decision_trace")
@Schema(description = "决议落地追踪记录")
public class MeetingDecisionTrace extends BaseEntity {

    @Schema(description = "决议ID")
    private Long decisionId;

    @Schema(description = "会议ID")
    private Long meetingId;

    @Schema(description = "追踪类型：1-进度更新 2-状态变更 3-备注补充 4-验证确认")
    private Integer traceType;

    @Schema(description = "更新内容")
    private String content;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "变更前状态")
    private Integer beforeStatus;

    @Schema(description = "变更后状态")
    private Integer afterStatus;

    @Schema(description = "变更前进度")
    private Integer beforeProgress;

    @Schema(description = "变更后进度")
    private Integer afterProgress;
}
