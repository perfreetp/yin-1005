package com.digitaltwin.pipeline.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline")
@Schema(description = "管线实体")
public class Pipeline extends BaseEntity {

    @Schema(description = "管线编号")
    private String pipelineCode;

    @Schema(description = "管线名称")
    private String pipelineName;

    @Schema(description = "管线类型：1-给水 2-排水 3-燃气 4-电力 5-通信 6-热力 7-工业")
    private Integer pipelineType;

    @Schema(description = "材质：1-铸铁 2-钢管 3-PE 4-PVC 5-混凝土 6-其他")
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

    @Schema(description = "运行状态：1-正常 2-维修中 3-停用 4-废弃")
    private Integer status;

    @Schema(description = "压力等级：1-低压 2-中压 3-次高压 4-高压")
    private Integer pressureLevel;

    @Schema(description = "断面占用信息")
    private String sectionInfo;

    @Schema(description = "备注")
    private String remark;
}
