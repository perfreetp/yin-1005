package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("defect_report")
@Schema(description = "缺陷上报")
public class DefectReport extends BaseEntity {

    @Schema(description = "缺陷编号")
    private String defectCode;

    @Schema(description = "缺陷标题")
    private String title;

    @Schema(description = "缺陷类型：1-管道腐蚀 2-管道泄漏 3-管道变形 4-井盖破损 5-井盖缺失 6-阀门故障 7-接口渗漏 8-沉降 9-占压 10-其他")
    private Integer defectType;

    @Schema(description = "缺陷等级：1-一般 2-较重 3-严重 4-紧急")
    private Integer defectLevel;

    @Schema(description = "缺陷描述")
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

    @Schema(description = "上报来源：1-巡检App 2-一张图平台 3-施工审批 4-群众举报 5-其他")
    private Integer reportSource;

    @Schema(description = "上报时间")
    private String reportTime;

    @Schema(description = "图片附件（JSON数组）")
    private String images;

    @Schema(description = "语音附件URL")
    private String voiceUrl;

    @Schema(description = "视频附件URL")
    private String videoUrl;

    @Schema(description = "缺陷状态：1-待受理 2-已受理 3-处理中 4-已修复 5-已验证 6-已归档 7-已驳回")
    private Integer status;

    @Schema(description = "受理人")
    private String receiver;

    @Schema(description = "受理时间")
    private String receiveTime;

    @Schema(description = "关联工单ID")
    private Long workOrderId;

    @Schema(description = "关联隐患点ID")
    private Long hazardId;

    @Schema(description = "备注")
    private String remark;
}
