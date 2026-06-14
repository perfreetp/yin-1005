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
@Schema(description = "统一位置信息（所有带位置的对象共用）")
public class LocationVO implements Serializable {

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "区域编码")
    private String areaCode;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "道路/街道名称")
    private String roadName;

    @Schema(description = "位置详细描述")
    private String address;

    @Schema(description = "影响半径（米，告警/事件附带）")
    private BigDecimal affectRadius;
}
