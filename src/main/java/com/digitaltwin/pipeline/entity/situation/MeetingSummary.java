package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meeting_summary")
@Schema(description = "处置纪要/会议纪要")
public class MeetingSummary extends BaseEntity {

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
