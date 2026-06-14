package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order_flow")
@Schema(description = "工单流转记录")
public class WorkOrderFlow extends BaseEntity {

    @Schema(description = "工单ID")
    private Long workOrderId;

    @Schema(description = "工单编号")
    private String orderCode;

    @Schema(description = "流转节点名称")
    private String nodeName;

    @Schema(description = "操作类型：1-创建 2-派单 3-接单 4-开始处理 5-上报进度 6-申请验收 7-验收 8-驳回 9-完成 10-取消")
    private Integer operationType;

    @Schema(description = "操作前状态")
    private Integer beforeStatus;

    @Schema(description = "操作后状态")
    private Integer afterStatus;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作人所属部门")
    private String operatorDept;

    @Schema(description = "操作时间")
    private String operateTime;

    @Schema(description = "操作意见")
    private String opinion;

    @Schema(description = "附件（JSON数组）")
    private String attachments;

    @Schema(description = "下一节点处理人")
    private String nextHandler;

    @Schema(description = "下一节点处理部门")
    private String nextHandlerDept;
}
