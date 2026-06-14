package com.digitaltwin.pipeline.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_valve")
@Schema(description = "阀门实体")
public class Valve extends BaseEntity {

    @Schema(description = "阀门编号")
    private String valveCode;

    @Schema(description = "阀门名称")
    private String valveName;

    @Schema(description = "阀门类型：1-闸阀 2-蝶阀 3-球阀 4-截止阀 5-止回阀")
    private Integer valveType;

    @Schema(description = "口径(mm)")
    private BigDecimal diameter;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "所属管线编号")
    private String pipelineCode;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "控制方向：1-正向 2-反向")
    private Integer controlDirection;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "安装位置")
    private String location;

    @Schema(description = "安装日期")
    private String installDate;

    @Schema(description = "产权单位")
    private String ownerUnit;

    @Schema(description = "运行状态：1-正常 2-关闭 3-故障 4-维修中")
    private Integer status;

    @Schema(description = "是否可远程控制：0-否 1-是")
    private Integer remoteControllable;

    @Schema(description = "备注")
    private String remark;
}
