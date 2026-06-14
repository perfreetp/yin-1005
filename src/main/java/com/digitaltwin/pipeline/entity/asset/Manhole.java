package com.digitaltwin.pipeline.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_manhole")
@Schema(description = "井盖实体")
public class Manhole extends BaseEntity {

    @Schema(description = "井盖编号")
    private String manholeCode;

    @Schema(description = "井盖名称")
    private String manholeName;

    @Schema(description = "井盖类型：1-雨水 2-污水 3-给水 4-电力 5-通信 6-燃气 7-热力 8-综合")
    private Integer manholeType;

    @Schema(description = "形状：1-圆形 2-方形 3-矩形")
    private Integer shape;

    @Schema(description = "规格尺寸")
    private String size;

    @Schema(description = "材质：1-铸铁 2-复合 3-水泥 4-其他")
    private Integer material;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "井室深度(m)")
    private BigDecimal depth;

    @Schema(description = "所属管线编号")
    private String pipelineCode;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "所在道路")
    private String roadName;

    @Schema(description = "安装日期")
    private String installDate;

    @Schema(description = "产权单位")
    private String ownerUnit;

    @Schema(description = "状态：1-正常 2-破损 3-缺失 4-维修中")
    private Integer status;

    @Schema(description = "是否有防坠网：0-否 1-是")
    private Integer hasSafetyNet;

    @Schema(description = "备注")
    private String remark;
}
