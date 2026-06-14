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
@Schema(description = "统一分页列表结果（列表+统计摘要，三端直接消费）")
public class UnifiedListResult<T> implements Serializable {

    @Schema(description = "列表数据")
    private List<T> list;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页")
    private Integer pageNum;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "总页数")
    private Integer pages;

    @Schema(description = "本页统计摘要")
    private ListSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "列表统计摘要")
    public static class ListSummary {
        @Schema(description = "总数")
        private Integer totalCount;

        @Schema(description = "紧急/最高级数量")
        private Integer criticalCount;

        @Schema(description = "高优先级数量")
        private Integer highCount;

        @Schema(description = "处理中数量")
        private Integer processingCount;

        @Schema(description = "已完成数量")
        private Integer completedCount;

        @Schema(description = "超期数量")
        private Integer overtimeCount;

        @Schema(description = "平均响应时长(分钟)")
        private BigDecimal avgResponseMinutes;

        @Schema(description = "平均处置时长(分钟)")
        private BigDecimal avgDisposalMinutes;
    }
}
