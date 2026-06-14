package com.digitaltwin.pipeline.entity.sensor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_hazard")
@Schema(description = "隐患点实体")
public class Hazard extends BaseEntity {

    @Schema(description = "隐患编号")
    private String hazardCode;

    @Schema(description = "隐患标题")
    private String title;

    @Schema(description = "隐患类型：1-腐蚀 2-泄漏 3-变形 4-沉降 5-占压 6-第三方破坏 7-材质老化 8-其他")
    private Integer hazardType;

    @Schema(description = "风险等级：1-低 2-中 3-高 4-极高")
    private Integer riskLevel;

    @Schema(description = "风险评分(0-100)")
    private Integer riskScore;

    @Schema(description = "隐患描述")
    private String description;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "位置描述")
    private String location;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "关联管线编号")
    private String pipelineCode;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "发现时间")
    private String discoverTime;

    @Schema(description = "发现人")
    private String discoverer;

    @Schema(description = "发现来源：1-巡检 2-告警 3-施工发现 4-群众举报 5-其他")
    private Integer discoverSource;

    @Schema(description = "影响范围描述")
    private String affectScope;

    @Schema(description = "紧急程度：1-一般 2-较重 3-严重 4-特别严重")
    private Integer urgency;

    @Schema(description = "建议处置措施")
    private String suggestion;

    @Schema(description = "图片附件（JSON数组）")
    private String images;

    @Schema(description = "隐患状态：1-待处置 2-处置中 3-已处置 4-已消除 5-持续监控")
    private Integer status;

    @Schema(description = "关联工单ID")
    private Long workOrderId;

    @Schema(description = "关联告警ID")
    private Long alarmId;

    @Schema(description = "备注")
    private String remark;
}
