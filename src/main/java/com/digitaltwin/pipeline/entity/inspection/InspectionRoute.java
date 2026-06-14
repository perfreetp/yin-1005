package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_route")
@Schema(description = "巡检路线")
public class InspectionRoute extends BaseEntity {

    @Schema(description = "路线编号")
    private String routeCode;

    @Schema(description = "路线名称")
    private String routeName;

    @Schema(description = "路线类型：1-日常巡检 2-专项巡检 3-重点巡检 4-应急巡检")
    private Integer routeType;

    @Schema(description = "管线类型：1-给水 2-排水 3-燃气 4-电力 5-通信 6-热力 7-全部")
    private Integer pipelineType;

    @Schema(description = "巡检点列表（JSON数组，含经纬度、顺序）")
    private String checkPoints;

    @Schema(description = "起点经度")
    private java.math.BigDecimal startLng;

    @Schema(description = "起点纬度")
    private java.math.BigDecimal startLat;

    @Schema(description = "终点经度")
    private java.math.BigDecimal endLng;

    @Schema(description = "终点纬度")
    private java.math.BigDecimal endLat;

    @Schema(description = "路线总长度(km)")
    private java.math.BigDecimal totalLength;

    @Schema(description = "预计巡检时长(分钟)")
    private Integer estimatedDuration;

    @Schema(description = "涉及管线ID列表（逗号分隔）")
    private String pipelineIds;

    @Schema(description = "涉及井盖ID列表（逗号分隔）")
    private String manholeIds;

    @Schema(description = "涉及阀门ID列表（逗号分隔）")
    private String valveIds;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "负责部门")
    private String responsibleDept;

    @Schema(description = "负责人员")
    private String responsiblePerson;

    @Schema(description = "巡检周期：1-每日 2-每周 3-每半月 4-每月 5-每季度 6-每年")
    private Integer cycleType;

    @Schema(description = "路线说明")
    private String description;

    @Schema(description = "状态：1-启用 2-停用")
    private Integer status;
}
