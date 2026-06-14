package com.digitaltwin.pipeline.dto.linkage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "联动任务详情结果（含完整溯源链）")
public class LinkedTaskDetailVO {

    @Schema(description = "任务基本信息")
    private TaskInfo task;

    @Schema(description = "触发来源业务快照")
    private SourceSnapshot source;

    @Schema(description = "流转记录时间线")
    private List<FlowRecord> flows;

    @Schema(description = "发送通知列表")
    private List<NotifyRecord> notifications;

    @Schema(description = "关联子工单列表")
    private List<RelatedWorkOrder> workOrders;

    @Schema(description = "关联巡检任务")
    private List<RelatedInspection> inspections;

    @Schema(description = "溯源链（从触发到当前的每一步进展）")
    private List<TraceNode> traceChain;

    @Data
    @Schema(description = "任务基本信息")
    public static class TaskInfo {
        private Long id;
        private String taskCode;
        private String title;
        private Integer taskType;
        private String taskTypeName;
        private Integer priority;
        private String priorityName;
        private String priorityColor;
        private Integer eventLevel;
        private String eventLevelName;
        private BigDecimal lng;
        private BigDecimal lat;
        private String areaCode;
        private String areaName;
        private Integer pipelineType;
        private String pipelineTypeName;
        private String undertakeDeptName;
        private String undertakerName;
        private String coDeptNames;
        private String description;
        private String disposalRequirement;
        private String deadline;
        private Integer status;
        private String statusName;
        private String currentNode;
        private Integer progress;
        private String actualFinishTime;
        private Integer usedMinutes;
    }

    @Data
    @Schema(description = "来源业务快照")
    public static class SourceSnapshot {
        private Integer sourceType;
        private String sourceTypeName;
        private Long sourceId;
        private String sourceCode;
        private String sourceTitle;
        private Integer sourceStatus;
        private String sourceStatusName;
        private String sourceDetailUrl;
        private String extraSummary;
    }

    @Data
    @Schema(description = "流转记录")
    public static class FlowRecord {
        private Long id;
        private Integer operationType;
        private String operationTypeName;
        private String operationNode;
        private String operationContent;
        private String operatorName;
        private String operatorDept;
        private String operationTime;
        private Integer beforeStatus;
        private String beforeStatusName;
        private Integer afterStatus;
        private String afterStatusName;
        private String remark;
    }

    @Data
    @Schema(description = "通知记录")
    public static class NotifyRecord {
        private Long id;
        private Integer notifyType;
        private String notifyTypeName;
        private String receiverName;
        private String title;
        private String content;
        private Integer level;
        private String levelName;
        private Integer sendStatus;
        private String sendStatusName;
        private String sendTime;
        private String readTime;
        private String confirmTime;
    }

    @Data
    @Schema(description = "关联工单")
    public static class RelatedWorkOrder {
        private Long id;
        private String orderCode;
        private String title;
        private Integer status;
        private String statusName;
        private Integer progress;
        private String undertaker;
        private String actualStartTime;
    }

    @Data
    @Schema(description = "关联巡检")
    public static class RelatedInspection {
        private Long id;
        private String routeCode;
        private String routeName;
        private Integer pointCount;
        private String inspector;
        private String status;
    }

    @Data
    @Schema(description = "溯源节点")
    public static class TraceNode {
        private Integer step;
        private String nodeName;
        private String time;
        private String operator;
        private String description;
        private String status;
    }
}
