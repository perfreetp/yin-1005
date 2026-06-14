package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_replay_analysis")
@Schema(description = "复盘分析")
public class PlanReplayAnalysis extends BaseEntity {

    @Schema(description = "执行记录ID")
    private Long executionId;

    @Schema(description = "执行记录编号")
    private String executionCode;

    @Schema(description = "方案准确度评分（0-100）")
    private Integer planAccuracyScore;

    @Schema(description = "时间偏差评分（0-100）")
    private Integer timeDeviationScore;

    @Schema(description = "影响偏差评分（0-100）")
    private Integer impactDeviationScore;

    @Schema(description = "操作准确度评分（0-100）")
    private Integer operationAccuracyScore;

    @Schema(description = "做得好的方面")
    private String goodPoints;

    @Schema(description = "需改进的方面")
    private String improvementPoints;

    @Schema(description = "优化建议")
    private String suggestions;

    @Schema(description = "经验教训")
    private String learnedLessons;

    @Schema(description = "复盘人姓名")
    private String reviewerName;

    @Schema(description = "复盘时间")
    private LocalDateTime reviewTime;
}
