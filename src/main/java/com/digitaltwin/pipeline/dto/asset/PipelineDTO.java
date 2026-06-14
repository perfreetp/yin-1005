package com.digitaltwin.pipeline.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "管线创建/更新DTO")
public class PipelineDTO {

    @Schema(description = "ID（更新时必填）")
    private Long id;

    @NotBlank(message = "管线编号不能为空")
    @Schema(description = "管线编号")
    private String pipelineCode;

    @NotBlank(message = "管线名称不能为空")
    @Schema(description = "管线名称")
    private String pipelineName;

    @NotNull(message = "管线类型不能为空")
    @Schema(description = "管线类型")
    private Integer pipelineType;

    @Schema(description = "材质")
    private Integer material;

    @Schema(description = "管径(mm)")
    private BigDecimal diameter;

    @Schema(description = "管长(m)")
    private BigDecimal length;

    @Schema(description = "埋设深度(m)")
    private BigDecimal buriedDepth;

    @Schema(description = "起始节点编号")
    private String startNodeCode;

    @Schema(description = "终止节点编号")
    private String endNodeCode;

    @Schema(description = "起点经度")
    private BigDecimal startLng;

    @Schema(description = "起点纬度")
    private BigDecimal startLat;

    @Schema(description = "终点经度")
    private BigDecimal endLng;

    @Schema(description = "终点纬度")
    private BigDecimal endLat;

    @Schema(description = "中心线坐标（WKT格式）")
    private String geometry;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "道路名称")
    private String roadName;

    @Schema(description = "建设年代")
    private String buildYear;

    @Schema(description = "产权单位")
    private String ownerUnit;

    @Schema(description = "运维单位")
    private String maintenanceUnit;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "压力等级")
    private Integer pressureLevel;

    @Schema(description = "断面占用信息")
    private String sectionInfo;

    @Schema(description = "备注")
    private String remark;
}
