package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一列表条目VO（告警/事件/工单/开挖/隐患 全部统一格式，三端直接用）")
public class UnifiedListItemVO implements Serializable {

    @Schema(description = "资源类型编码（见 ResourceTypeEnum）")
    private Integer resourceType;

    @Schema(description = "资源类型名称")
    private String resourceTypeName;

    @Schema(description = "业务ID")
    private Long id;

    @Schema(description = "业务编号")
    private String code;

    @Schema(description = "标题/名称")
    private String title;

    @Schema(description = "管线类型（关联）")
    private Integer pipelineType;

    @Schema(description = "管线类型名称")
    private String pipelineTypeName;

    @Schema(description = "优先级/级别（1-4）")
    private Integer priority;

    @Schema(description = "优先级标签（含颜色）")
    private StatusTagVO priorityTag;

    @Schema(description = "当前状态标签（含颜色+进度）")
    private StatusTagVO statusTag;

    @Schema(description = "位置信息")
    private LocationVO location;

    @Schema(description = "影响信息")
    private ImpactInfoVO impact;

    @Schema(description = "承办/责任人")
    private HandlerInfoVO handler;

    @Schema(description = "时间信息")
    private TimelineInfoVO timeline;

    @Schema(description = "标签列表（前端展示用）")
    private List<String> tags;

    @Schema(description = "来源ID（联动溯源用）")
    private Long sourceId;

    @Schema(description = "来源类型")
    private Integer sourceType;

    @Schema(description = "来源名称")
    private String sourceName;
}
