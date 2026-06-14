package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一枚举值对象（三端共用，不再各自转换）")
public class EnumValueVO implements Serializable {

    @Schema(description = "编码（Integer 类型）")
    private Integer code;

    @Schema(description = "字符串 Key（用于前端 class/style）")
    private String key;

    @Schema(description = "中文显示名")
    private String label;

    @Schema(description = "推荐颜色（#RRGGBB）")
    private String color;
}
