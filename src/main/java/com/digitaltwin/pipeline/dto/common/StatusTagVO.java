package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一状态标签VO（三端共用：状态码+英文名+中文名+颜色）")
public class StatusTagVO implements Serializable {

    @Schema(description = "状态编码")
    private Integer code;

    @Schema(description = "英文标识（前端做class/style映射）")
    private String key;

    @Schema(description = "中文显示名")
    private String label;

    @Schema(description = "主题色（#RRGGBB）")
    private String color;

    @Schema(description = "进度百分比，0-100（流程型状态附带）")
    private Integer progress;
}
