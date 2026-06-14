package com.digitaltwin.pipeline.dto.situation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "指挥大屏多事件对比结果")
public class CommandDashboardVO implements Serializable {

    @Schema(description = "刷新时间")
    private String refreshTime;

    @Schema(description = "全局概览指标")
    private GlobalOverview overview;

    @Schema(description = "事件对比列表（并排对比）")
    private List<EventCompareItem> events;

    @Schema(description = "排行榜/优先级排序")
    private List<RankingItem> rankings;

    @Schema(description = "时间轴视图（所有事件叠加展示）")
    private List<TimelineBand> timelineBands;

    @Schema(description = "按区域统计对比")
    private List<AreaCompareItem> areaCompare;

    @Schema(description = "按管线类型统计对比")
    private List<PipelineTypeCompareItem> pipelineTypeCompare;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "全局概览")
    public static class GlobalOverview {
        @Schema(description = "进行中事件数")
        private Integer activeEventCount;
        @Schema(description = "处置中工单数")
        private Integer activeWorkOrderCount;
        @Schema(description = "待响应告警数")
        private Integer pendingAlarmCount;
        @Schema(description = "施工中开挖数")
        private Integer constructingCount;
        @Schema(description = "最高级别事件")
        private String highestLevel;
        @Schema(description = "最高级别事件颜色")
        private String highestLevelColor;
        @Schema(description = "整体态势评分")
        private Integer overallScore;
        @Schema(description = "整体态势等级")
        private String overallLevel;
        @Schema(description = "需关注事件数（2级及以上且未处置）")
        private Integer needAttentionCount;
        @Schema(description = "今日新增事件数")
        private Integer todayNewCount;
        @Schema(description = "今日已完成事件数")
        private Integer todayClosedCount;
        @Schema(description = "平均响应时长(分钟)")
        private BigDecimal avgResponseMinutes;
        @Schema(description = "平均处置时长(分钟)")
        private BigDecimal avgDisposalMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "事件对比卡片")
    public static class EventCompareItem {
        @Schema(description = "事件ID")
        private Long eventId;
        @Schema(description = "事件编号")
        private String eventCode;
        @Schema(description = "事件标题")
        private String title;
        @Schema(description = "事件类型")
        private Integer eventType;
        @Schema(description = "事件类型名称")
        private String eventTypeName;
        @Schema(description = "事件级别")
        private Integer eventLevel;
        @Schema(description = "事件级别名称")
        private String eventLevelName;
        @Schema(description = "事件级别颜色")
        private String eventLevelColor;
        @Schema(description = "当前状态")
        private Integer status;
        @Schema(description = "当前状态名称")
        private String statusName;
        @Schema(description = "当前阶段")
        private String currentStage;
        @Schema(description = "进度百分比")
        private Integer progress;
        @Schema(description = "区域")
        private String areaName;
        @Schema(description = "管线类型")
        private String pipelineTypeName;
        @Schema(description = "位置坐标")
        private BigDecimal lng;
        private BigDecimal lat;
        @Schema(description = "响应耗时(分钟)")
        private Integer responseMinutes;
        @Schema(description = "响应耗时等级：1-快 2-正常 3-慢 4-超时")
        private Integer responseSpeedLevel;
        @Schema(description = "处置已用时长(分钟)")
        private Integer disposalMinutes;
        @Schema(description = "预计剩余时长(分钟)")
        private Integer remainingMinutes;
        @Schema(description = "影响用户数")
        private Integer affectedUsers;
        @Schema(description = "影响企业数")
        private Integer affectedEnterprises;
        @Schema(description = "影响道路数")
        private Integer affectedRoads;
        @Schema(description = "停水停气范围描述")
        private String outageScope;
        @Schema(description = "恢复进度%")
        private Integer recoveryProgress;
        @Schema(description = "已恢复用户数")
        private Integer recoveredUsers;
        @Schema(description = "承办部门")
        private String undertakeDept;
        @Schema(description = "现场负责人")
        private String fieldLeader;
        @Schema(description = "参与部门数")
        private Integer involvedDeptCount;
        @Schema(description = "发现时间")
        private String discoverTime;
        @Schema(description = "持续时长描述")
        private String durationText;
        @Schema(description = "优先级排序分值")
        private Integer priorityScore;
        @Schema(description = "标签")
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "优先级排行")
    public static class RankingItem {
        @Schema(description = "排名")
        private Integer rank;
        @Schema(description = "事件ID")
        private Long eventId;
        @Schema(description = "事件标题")
        private String title;
        @Schema(description = "事件级别")
        private Integer level;
        @Schema(description = "级别颜色")
        private String levelColor;
        @Schema(description = "综合优先级分值")
        private Integer score;
        @Schema(description = "主要关注点")
        private String focusPoint;
        @Schema(description = "建议动作")
        private String suggestedAction;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "时间轴带（每个事件一条横向带）")
    public static class TimelineBand {
        @Schema(description = "事件ID")
        private Long eventId;
        @Schema(description = "事件名称")
        private String eventName;
        @Schema(description = "级别颜色")
        private String levelColor;
        @Schema(description = "起始时间偏移(分钟，相对于当天0点)")
        private Integer startOffset;
        @Schema(description = "持续时长(分钟)")
        private Integer duration;
        @Schema(description = "进度%")
        private Integer progress;
        @Schema(description = "关键节点列表")
        private List<KeyNode> keyNodes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "关键节点")
    public static class KeyNode {
        @Schema(description = "节点类型")
        private Integer nodeType;
        @Schema(description = "节点名称")
        private String nodeName;
        @Schema(description = "时间偏移(分钟)")
        private Integer timeOffset;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "区域对比")
    public static class AreaCompareItem {
        private String areaCode;
        private String areaName;
        private Integer eventCount;
        private Integer alarmCount;
        private Integer workOrderCount;
        private Integer affectedUsers;
        private Integer level;
        private String levelName;
        private String levelColor;
        private BigDecimal avgResponseMinutes;
        private BigDecimal avgDisposalMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "管线类型对比")
    public static class PipelineTypeCompareItem {
        private Integer pipelineType;
        private String typeName;
        private Integer eventCount;
        private Integer alarmCount;
        private Integer affectedUsers;
        private String typeColor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "事件钻取详情")
    public static class EventDrillDownVO {
        @Schema(description = "事件ID")
        private Long eventId;
        @Schema(description = "事件编号")
        private String eventCode;
        @Schema(description = "事件标题")
        private String title;
        @Schema(description = "事件类型")
        private Integer eventType;
        @Schema(description = "事件类型名称")
        private String eventTypeName;
        @Schema(description = "事件级别")
        private Integer level;
        @Schema(description = "事件级别名称")
        private String levelName;
        @Schema(description = "事件级别颜色")
        private String levelColor;
        @Schema(description = "当前状态")
        private Integer status;
        @Schema(description = "当前状态名称")
        private String statusName;
        @Schema(description = "进度百分比")
        private Integer progress;
        @Schema(description = "区域名称")
        private String areaName;
        @Schema(description = "位置坐标")
        private BigDecimal lng;
        private BigDecimal lat;
        @Schema(description = "发现时间")
        private String discoverTime;
        @Schema(description = "持续时长描述")
        private String durationText;
        @Schema(description = "受影响管线列表")
        private List<DrillPipelineVO> affectedPipelines;
        @Schema(description = "相关告警列表")
        private List<DrillAlarmVO> relatedAlarms;
        @Schema(description = "正在执行的工单列表")
        private List<DrillWorkOrderVO> activeWorkOrders;
        @Schema(description = "最近一次会商记录")
        private DrillMeetingVO latestMeeting;
        @Schema(description = "受影响阀门列表")
        private List<DrillValveVO> affectedValves;
        @Schema(description = "钻取汇总")
        private DrillSummaryVO drillSummary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "受影响管线")
    public static class DrillPipelineVO {
        @Schema(description = "管线ID")
        private Long pipelineId;
        @Schema(description = "管线编号")
        private String pipelineCode;
        @Schema(description = "管线类型")
        private Integer pipelineType;
        @Schema(description = "类型名称")
        private String typeName;
        @Schema(description = "起点")
        private String startPoint;
        @Schema(description = "终点")
        private String endPoint;
        @Schema(description = "长度(米)")
        private BigDecimal lengthMeters;
        @Schema(description = "管径(mm)")
        private Integer diameter;
        @Schema(description = "材质")
        private String material;
        @Schema(description = "状态")
        private Integer status;
        @Schema(description = "状态名称")
        private String statusName;
        @Schema(description = "距离事件点距离(米)")
        private BigDecimal distanceFromEvent;
        @Schema(description = "是否主管线")
        private Boolean isMainLine;
        @Schema(description = "影响程度：1-轻微 2-中等 3-严重")
        private Integer affectedDegree;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "相关告警")
    public static class DrillAlarmVO {
        @Schema(description = "告警ID")
        private Long alarmId;
        @Schema(description = "告警编号")
        private String alarmCode;
        @Schema(description = "告警类型")
        private Integer alarmType;
        @Schema(description = "类型名称")
        private String typeName;
        @Schema(description = "告警级别")
        private Integer level;
        @Schema(description = "级别名称")
        private String levelName;
        @Schema(description = "级别颜色")
        private String levelColor;
        @Schema(description = "状态")
        private Integer status;
        @Schema(description = "状态名称")
        private String statusName;
        @Schema(description = "告警时间")
        private String alarmTime;
        @Schema(description = "位置描述")
        private String locationDescription;
        @Schema(description = "传感器编号")
        private String sensorCode;
        @Schema(description = "告警值")
        private BigDecimal value;
        @Schema(description = "阈值")
        private BigDecimal threshold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "执行中工单")
    public static class DrillWorkOrderVO {
        @Schema(description = "工单ID")
        private Long workOrderId;
        @Schema(description = "工单编号")
        private String workOrderCode;
        @Schema(description = "工单标题")
        private String title;
        @Schema(description = "状态")
        private Integer status;
        @Schema(description = "状态名称")
        private String statusName;
        @Schema(description = "进度百分比")
        private Integer progress;
        @Schema(description = "优先级")
        private Integer priority;
        @Schema(description = "优先级名称")
        private String priorityName;
        @Schema(description = "处理人")
        private String assignee;
        @Schema(description = "处理部门")
        private String assigneeDept;
        @Schema(description = "创建时间")
        private String createTime;
        @Schema(description = "预计完成时间")
        private String estimatedFinishTime;
        @Schema(description = "当前位置描述")
        private String currentLocation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最近会商")
    public static class DrillMeetingVO {
        @Schema(description = "会议ID")
        private Long meetingId;
        @Schema(description = "会议编号")
        private String meetingCode;
        @Schema(description = "会议标题")
        private String title;
        @Schema(description = "会议类型")
        private Integer meetingType;
        @Schema(description = "类型名称")
        private String typeName;
        @Schema(description = "状态")
        private Integer status;
        @Schema(description = "状态名称")
        private String statusName;
        @Schema(description = "开始时间")
        private String startTime;
        @Schema(description = "持续时长(分钟)")
        private Integer durationMinutes;
        @Schema(description = "参会人数")
        private Integer attendeeCount;
        @Schema(description = "决策数量")
        private Integer decisionCount;
        @Schema(description = "最近一条决策摘要")
        private String latestDecision;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "受影响阀门")
    public static class DrillValveVO {
        @Schema(description = "阀门ID")
        private Long valveId;
        @Schema(description = "阀门编号")
        private String valveCode;
        @Schema(description = "阀门名称")
        private String valveName;
        @Schema(description = "阀门类型")
        private Integer valveType;
        @Schema(description = "类型名称")
        private String typeName;
        @Schema(description = "当前状态：1-开 2-关")
        private Integer currentStatus;
        @Schema(description = "状态名称")
        private String statusName;
        @Schema(description = "管径(mm)")
        private Integer diameter;
        @Schema(description = "位置描述")
        private String location;
        @Schema(description = "距离事件点距离(米)")
        private BigDecimal distanceFromEvent;
        @Schema(description = "操作状态：0-正常 1-待操作 2-已操作")
        private Integer operationStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "钻取汇总")
    public static class DrillSummaryVO {
        @Schema(description = "受影响管线数")
        private Integer affectedPipelineCount;
        @Schema(description = "受影响阀门数")
        private Integer affectedValveCount;
        @Schema(description = "活跃告警数")
        private Integer activeAlarmCount;
        @Schema(description = "活跃工单数")
        private Integer activeWorkOrderCount;
        @Schema(description = "总影响用户数")
        private Integer totalAffectedUsers;
        @Schema(description = "预计恢复时间(分钟)")
        private Integer estimatedRecoveryTime;
        @Schema(description = "整体风险等级")
        private Integer overallRiskLevel;
        @Schema(description = "风险等级名称")
        private String levelName;
        @Schema(description = "风险等级颜色")
        private String levelColor;
    }
}
