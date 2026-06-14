package com.digitaltwin.pipeline.dto.inspection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "缺陷上报DTO")
public class DefectReportDTO {

    @NotBlank(message = "缺陷标题不能为空")
    @Schema(description = "缺陷标题")
    private String title;

    @NotNull(message = "缺陷类型不能为空")
    @Schema(description = "缺陷类型")
    private Integer defectType;

    @Schema(description = "缺陷等级")
    private Integer defectLevel;

    @Schema(description = "缺陷描述")
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

    @Schema(description = "关联井盖ID")
    private Long manholeId;

    @Schema(description = "关联阀门ID")
    private Long valveId;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "关联巡检记录ID")
    private Long inspectionRecordId;

    @Schema(description = "上报人ID")
    private Long reporterId;

    @Schema(description = "上报人姓名")
    private String reporterName;

    @Schema(description = "上报来源")
    private Integer reportSource;

    @Schema(description = "图片附件")
    private String images;

    @Schema(description = "语音附件URL")
    private String voiceUrl;

    @Schema(description = "视频附件URL")
    private String videoUrl;
}
