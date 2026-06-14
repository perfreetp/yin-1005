package com.digitaltwin.pipeline.entity.linkage;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("linked_task_flow")
@Schema(description = "联动任务流转记录")
public class LinkedTaskFlow extends BaseEntity {

    @Schema(description = "联动任务ID")
    private Long taskId;

    @Schema(description = "联动任务编号")
    private String taskCode;

    @Schema(description = "操作类型：1-创建 2-派单 3-开始 4-上报进度 5-申请验收 6-验收通过 7-驳回 8-完成 9-取消 10-通知发送")
    private Integer operationType;

    @Schema(description = "操作节点名称")
    private String operationNode;

    @Schema(description = "操作说明")
    private String operationContent;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作人部门")
    private String operatorDept;

    @Schema(description = "操作时间")
    private String operationTime;

    @Schema(description = "操作前状态")
    private Integer beforeStatus;

    @Schema(description = "操作后状态")
    private Integer afterStatus;

    @Schema(description = "附件图片，JSON数组")
    private String images;

    @Schema(description = "备注")
    private String remark;
}
