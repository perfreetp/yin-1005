package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一影响评估（事件/告警/开挖 通用）")
public class ImpactInfoVO implements Serializable {

    @Schema(description = "影响等级：1-轻微 2-一般 3-较大 4-重大")
    private Integer level;

    @Schema(description = "影响等级名称")
    private String levelName;

    @Schema(description = "影响等级颜色")
    private String levelColor;

    @Schema(description = "影响半径（米）")
    private BigDecimal radius;

    @Schema(description = "预计影响用户数")
    private Integer affectedUsers;

    @Schema(description = "预计影响企业数")
    private Integer affectedEnterprises;

    @Schema(description = "影响道路数量")
    private Integer affectedRoads;

    @Schema(description = "影响范围文字描述")
    private String scopeDescription;
}
