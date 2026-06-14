package com.digitaltwin.pipeline.entity.construction;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("excavation_application")
@Schema(description = "开挖申请")
public class ExcavationApplication extends BaseEntity {

    @Schema(description = "申请编号")
    private String applicationCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "申请单位")
    private String applicantUnit;

    @Schema(description = "申请人")
    private String applicant;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "施工单位")
    private String constructionUnit;

    @Schema(description = "施工负责人")
    private String constructionLeader;

    @Schema(description = "开挖类型：1-道路开挖 2-绿化开挖 3-建筑施工 4-管线铺设 5-其他")
    private Integer excavationType;

    @Schema(description = "开挖区域描述")
    private String areaDescription;

    @Schema(description = "开挖区域边界坐标（WKT格式多边形）")
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

    @Schema(description = "涉及管线数量")
    private Integer involvedPipelineCount;

    @Schema(description = "是否存在冲突：0-否 1-是")
    private Integer hasConflict;

    @Schema(description = "冲突管线类型（逗号分隔）")
    private String conflictPipelineTypes;

    @Schema(description = "影响道路数量")
    private Integer affectedRoadCount;

    @Schema(description = "影响道路名称（逗号分隔）")
    private String affectedRoadNames;

    @Schema(description = "申请状态：1-待提交 2-待校核 3-校核通过 4-校核不通过 5-待审批 6-审批通过 7-审批不通过 8-已开工 9-已完工 10-已归档")
    private Integer status;

    @Schema(description = "校核意见")
    private String reviewOpinion;

    @Schema(description = "校核人")
    private String reviewer;

    @Schema(description = "校核时间")
    private String reviewTime;

    @Schema(description = "审批意见")
    private String approvalOpinion;

    @Schema(description = "审批人")
    private String approver;

    @Schema(description = "审批时间")
    private String approvalTime;

    @Schema(description = "附件（JSON数组）")
    private String attachments;

    @Schema(description = "备注")
    private String remark;
}
