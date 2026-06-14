package com.digitaltwin.pipeline.entity.construction;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("emergency_valve_suggestion")
@Schema(description = "应急关阀建议")
public class EmergencyValveSuggestion extends BaseEntity {

    @Schema(description = "建议编号")
    private String suggestionCode;

    @Schema(description = "关联开挖申请ID")
    private Long applicationId;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "关联管线编号")
    private String pipelineCode;

    @Schema(description = "关联管线名称")
    private String pipelineName;

    @Schema(description = "关阀原因：1-施工防护 2-抢修作业 3-应急处置 4-其他")
    private Integer reasonType;

    @Schema(description = "建议关闭阀门ID列表（JSON数组）")
    private String valveIds;

    @Schema(description = "建议关闭阀门编号列表（逗号分隔）")
    private String valveCodes;

    @Schema(description = "建议关闭阀门名称列表（逗号分隔）")
    private String valveNames;

    @Schema(description = "受影响管线数量")
    private Integer affectedPipelineCount;

    @Schema(description = "受影响区域描述")
    private String affectedArea;

    @Schema(description = "预计影响用户数")
    private Integer estimatedAffectedUsers;

    @Schema(description = "预计影响时长(小时)")
    private Integer estimatedDuration;

    @Schema(description = "关阀操作步骤说明")
    private String operationSteps;

    @Schema(description = "安全注意事项")
    private String safetyNotes;

    @Schema(description = "恢复供气/供水方案")
    private String recoveryPlan;

    @Schema(description = "建议状态：1-建议 2-已确认 3-已执行 4-已取消")
    private Integer status;

    @Schema(description = "确认人")
    private String confirmer;

    @Schema(description = "确认时间")
    private String confirmTime;

    @Schema(description = "执行人")
    private String executor;

    @Schema(description = "执行时间")
    private String executeTime;

    @Schema(description = "备注")
    private String remark;
}
