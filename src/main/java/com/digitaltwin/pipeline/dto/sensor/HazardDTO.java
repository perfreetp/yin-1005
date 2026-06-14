package com.digitaltwin.pipeline.dto.sensor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "隐患点创建DTO")
public class HazardDTO {

    @Schema(description = "ID（更新时必填）")
    private Long id;

    @NotBlank(message = "隐患标题不能为空")
    @Schema(description = "隐患标题")
    private String title;

    @NotNull(message = "隐患类型不能为空")
    @Schema(description = "隐患类型")
    private Integer hazardType;

    @Schema(description = "风险等级")
    private Integer riskLevel;

    @Schema(description = "风险评分(0-100)")
    private Integer riskScore;

    @Schema(description = "隐患描述")
    private String description;

    @NotNull(message = "经度不能为空")
    @Schema(description = "经度")
    private BigDecimal lng;

    @NotNull(message = "纬度不能为空")
    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "位置描述")
    private String location;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "发现来源")
    private Integer discoverSource;

    @Schema(description = "影响范围描述")
    private String affectScope;

    @Schema(description = "紧急程度")
    private Integer urgency;

    @Schema(description = "建议处置措施")
    private String suggestion;

    @Schema(description = "图片附件")
    private String images;
}
