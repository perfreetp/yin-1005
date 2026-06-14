package com.digitaltwin.pipeline.dto.construction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "开挖申请创建DTO")
public class ExcavationDTO {

    @Schema(description = "ID（更新时必填）")
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    @Schema(description = "项目名称")
    private String projectName;

    @NotBlank(message = "申请单位不能为空")
    @Schema(description = "申请单位")
    private String applicantUnit;

    @NotBlank(message = "申请人不能为空")
    @Schema(description = "申请人")
    private String applicant;

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "施工单位")
    private String constructionUnit;

    @Schema(description = "施工负责人")
    private String constructionLeader;

    @NotNull(message = "开挖类型不能为空")
    @Schema(description = "开挖类型")
    private Integer excavationType;

    @NotBlank(message = "开挖区域描述不能为空")
    @Schema(description = "开挖区域描述")
    private String areaDescription;

    @Schema(description = "开挖区域边界坐标（WKT格式）")
    private String geometry;

    @Schema(description = "中心点经度")
    private BigDecimal centerLng;

    @Schema(description = "中心点纬度")
    private BigDecimal centerLat;

    @Schema(description = "开挖面积(㎡)")
    private BigDecimal area;

    @Schema(description = "开挖深度(m)")
    private BigDecimal depth;

    @Schema(description = "计划开工日期")
    private String planStartDate;

    @Schema(description = "计划完工日期")
    private String planEndDate;

    @Schema(description = "施工内容描述")
    private String constructionContent;

    @Schema(description = "防护措施说明")
    private String protectionMeasures;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "附件")
    private String attachments;

    @Schema(description = "备注")
    private String remark;
}
