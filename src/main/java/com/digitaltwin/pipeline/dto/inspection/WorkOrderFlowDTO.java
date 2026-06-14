package com.digitaltwin.pipeline.dto.inspection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "工单流转DTO")
public class WorkOrderFlowDTO {

    @Schema(description = "工单ID")
    private Long workOrderId;

    @Schema(description = "操作类型")
    private Integer operationType;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作人所属部门")
    private String operatorDept;

    @Schema(description = "操作意见")
    private String opinion;

    @Schema(description = "附件")
    private String attachments;

    @Schema(description = "下一节点处理人")
    private String nextHandler;

    @Schema(description = "下一节点处理部门")
    private String nextHandlerDept;

    @Schema(description = "承办部门")
    private String undertakeDept;

    @Schema(description = "承办人")
    private String undertaker;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "预计完成时间")
    private String expectCompleteTime;

    @Schema(description = "处置方案说明")
    private String disposalPlan;

    @Schema(description = "处置过程描述")
    private String disposalProcess;

    @Schema(description = "处置结果说明")
    private String disposalResult;

    @Schema(description = "验收意见")
    private String acceptOpinion;

    @Schema(description = "验收是否合格")
    private Integer acceptQualified;

    @Schema(description = "施工前图片")
    private String beforeImages;

    @Schema(description = "施工中图片")
    private String duringImages;

    @Schema(description = "施工后图片")
    private String afterImages;
}
