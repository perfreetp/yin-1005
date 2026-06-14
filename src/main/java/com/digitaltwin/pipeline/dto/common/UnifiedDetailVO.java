package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一详情VO（告警/事件/工单/开挖/隐患 全部统一格式，三端直接用，和列表项字段一一对应）")
public class UnifiedDetailVO implements Serializable {

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

    @Schema(description = "详情扩展字段（不同业务类型填充不同内容，前端按需展示）")
    private DetailExtraVO extra;

    @Schema(description = "关联资源列表（相关告警、工单、开挖等）")
    private List<RelatedResourceVO> relatedResources;

    @Schema(description = "时间线（最近N条）")
    private List<UnifiedTimelineItemVO> timelineItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "详情扩展字段")
    public static class DetailExtraVO {
        @Schema(description = "描述/内容")
        private String description;

        @Schema(description = "数值（告警值/压力值/液位等）")
        private BigDecimal value;

        @Schema(description = "阈值")
        private BigDecimal threshold;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "传感器编号/设备编号")
        private String deviceCode;

        @Schema(description = "施工单位（开挖用）")
        private String constructionUnit;

        @Schema(description = "施工负责人（开挖用）")
        private String constructionLeader;

        @Schema(description = "联系电话")
        private String contactPhone;

        @Schema(description = "风险评分")
        private Integer riskScore;

        @Schema(description = "隐患类型（隐患用）")
        private String hazardType;

        @Schema(description = "缺陷描述（工单/隐患用）")
        private String defectDesc;

        @Schema(description = "当前阶段")
        private String currentStage;

        @Schema(description = "进展描述")
        private String progressDesc;

        @Schema(description = "预计费用")
        private BigDecimal estimatedCost;

        @Schema(description = "实际费用")
        private BigDecimal actualCost;

        @Schema(description = "参与部门数")
        private Integer involvedDeptCount;

        @Schema(description = "现场人数")
        private Integer fieldPersonCount;

        @Schema(description = "自定义扩展属性（KV结构，按需放各种业务字段）")
        private Map<String, Object> attributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "关联资源")
    public static class RelatedResourceVO {
        @Schema(description = "资源类型")
        private Integer resourceType;

        @Schema(description = "资源类型名称")
        private String resourceTypeName;

        @Schema(description = "资源ID")
        private Long resourceId;

        @Schema(description = "资源编号")
        private String resourceCode;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "状态标签")
        private StatusTagVO statusTag;

        @Schema(description = "优先级标签")
        private StatusTagVO priorityTag;

        @Schema(description = "关联关系描述")
        private String relationDesc;
    }
}
