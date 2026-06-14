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
}
