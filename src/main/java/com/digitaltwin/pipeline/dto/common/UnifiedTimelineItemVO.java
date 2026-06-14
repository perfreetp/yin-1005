package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一时间线条目VO（变更/流转/日志 通用）")
public class UnifiedTimelineItemVO implements Serializable {

    @Schema(description = "时间节点ID")
    private Long id;

    @Schema(description = "节点类型")
    private Integer pointType;

    @Schema(description = "节点类型名称")
    private String pointTypeName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "详情描述")
    private String description;

    @Schema(description = "发生时间")
    private String occurTime;

    @Schema(description = "相对起始的秒数（时间轴渲染用）")
    private Long relativeSeconds;

    @Schema(description = "操作持续分钟数")
    private Integer durationMinutes;

    @Schema(description = "操作人信息")
    private HandlerInfoVO operator;

    @Schema(description = "操作后状态标签")
    private StatusTagVO statusAfter;

    @Schema(description = "重要程度：1-轻微 2-一般 3-重要 4-关键")
    private Integer importance;

    @Schema(description = "关联资源ID")
    private Long relatedResourceId;

    @Schema(description = "关联资源类型")
    private Integer relatedResourceType;

    @Schema(description = "关联资源编号")
    private String relatedResourceCode;

    @Schema(description = "附件/图片列表")
    private List<String> attachments;

    @Schema(description = "备注")
    private String remark;
}
