package com.digitaltwin.pipeline.dto.construction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "开挖校核结果DTO")
public class ExcavationReviewResultDTO {

    @Schema(description = "是否通过校核")
    private Boolean passed;

    @Schema(description = "涉及管线数量")
    private Integer involvedPipelineCount;

    @Schema(description = "冲突管线数量")
    private Integer conflictCount;

    @Schema(description = "管线冲突列表")
    private List<com.digitaltwin.pipeline.entity.construction.PipelineConflict> conflicts;

    @Schema(description = "影响道路数量")
    private Integer affectedRoadCount;

    @Schema(description = "影响道路名称列表")
    private List<String> affectedRoadNames;

    @Schema(description = "应急关阀建议")
    private List<com.digitaltwin.pipeline.entity.construction.EmergencyValveSuggestion> valveSuggestions;

    @Schema(description = "综合评估意见")
    private String reviewOpinion;
}
