package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("event_incident")
@Schema(description = "事件记录：用于态势视图和历史回放的主表")
public class EventIncident extends BaseEntity {

    @Schema(description = "事件编号，格式 EV-yyyyMMdd-xxxx")
    private String eventCode;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件类型：1-开挖施工 2-告警 3-隐患 4-巡检发现 5-泄漏爆管 6-占道 7-多源综合 8-其他")
    private Integer eventType;

    @Schema(description = "事件级别：1-提示 2-预警 3-事件 4-紧急 5-重大")
    private Integer eventLevel;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "影响半径（米）")
    private BigDecimal affectRadius;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "涉及管线类型")
    private Integer pipelineType;

    @Schema(description = "事件来源ID列表，逗号分隔")
    private String sourceIds;

    @Schema(description = "事件描述")
    private String description;

    @Schema(description = "发现时间")
    private String discoverTime;

    @Schema(description = "首次响应时间")
    private String firstResponseTime;

    @Schema(description = "开始处置时间")
    private String startDisposalTime;

    @Schema(description = "处置完成时间")
    private String finishDisposalTime;

    @Schema(description = "恢复时间")
    private String recoverTime;

    @Schema(description = "总耗时(分钟)")
    private Integer usedMinutes;

    @Schema(description = "事件状态：1-待响应 2-处置中 3-已控制 4-已处置 5-已恢复 6-已归档")
    private Integer status;

    @Schema(description = "当前处理阶段")
    private String currentStage;

    @Schema(description = "处置进度%")
    private Integer progress;

    @Schema(description = "直接责任人")
    private String responsiblePerson;

    @Schema(description = "指挥人员")
    private String commander;

    @Schema(description = "参与部门数")
    private Integer involvedDeptCount;

    @Schema(description = "预计影响用户数")
    private Integer estimatedAffectedUsers;

    @Schema(description = "实际影响用户数")
    private Integer actualAffectedUsers;

    @Schema(description = "是否已发布：0-否 1-是")
    private Integer published;

    @Schema(description = "备注")
    private String remark;
}
