package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_vehicle")
@Schema(description = "巡检车辆")
public class InspectionVehicle extends BaseEntity {

    @Schema(description = "车牌号")
    private String plateNumber;

    @Schema(description = "车辆类型：1-巡检车 2-抢修车 3-工程车")
    private Integer vehicleType;

    @Schema(description = "所属班组ID")
    private Long teamId;

    @Schema(description = "状态：1-可用 2-使用中 3-维修")
    private Integer status;

    @Schema(description = "载重(kg)")
    private Integer loadCapacity;

    @Schema(description = "当前经度")
    private BigDecimal currentLng;

    @Schema(description = "当前纬度")
    private BigDecimal currentLat;
}
