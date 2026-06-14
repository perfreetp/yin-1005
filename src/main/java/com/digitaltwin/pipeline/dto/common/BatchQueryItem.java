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
@Schema(description = "批量查询单条请求")
public class BatchQueryItem implements Serializable {

    @Schema(description = "资源类型（见 ResourceTypeEnum）", required = true)
    private Integer resourceType;

    @Schema(description = "资源ID", required = true)
    private Long resourceId;

    @Schema(description = "资源编号（和ID二选一，ID优先）")
    private String resourceCode;
}
