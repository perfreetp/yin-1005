package com.digitaltwin.pipeline.service.common;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.common.*;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.entity.situation.EventIncident;
import com.digitaltwin.pipeline.enums.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UnifiedVOBuilder {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static UnifiedListItemVO fromAlarm(Alarm alarm) {
        if (alarm == null) return null;
        UnifiedListItemVO vo = UnifiedListItemVO.builder().build();
        vo.setResourceType(5);
        vo.setResourceTypeName(ResourceTypeEnum.getLabel(5));
        vo.setId(alarm.getId());
        vo.setCode(alarm.getAlarmCode());
        vo.setTitle(alarm.getTitle());
        vo.setPipelineType(alarm.getPipelineType());
        vo.setPipelineTypeName(PipelineTypeEnum.getLabel(alarm.getPipelineType()));
        vo.setPriority(alarm.getAlarmLevel());
        vo.setPriorityTag(priorityTag(alarm.getAlarmLevel()));
        vo.setStatusTag(statusTag(alarm.getStatus(), "alarm"));
        vo.setLocation(LocationVO.builder()
                .lng(alarm.getLng())
                .lat(alarm.getLat())
                .areaCode(alarm.getAreaCode())
                .build());
        vo.setImpact(ImpactInfoVO.builder()
                .level(alarm.getAlarmLevel())
                .levelName(PriorityLevelEnum.getLabel(alarm.getAlarmLevel()))
                .levelColor(PriorityLevelEnum.getColor(alarm.getAlarmLevel()))
                .build());
        vo.setTimeline(TimelineInfoVO.builder()
                .createTime(alarm.getAlarmTime())
                .updateTime(alarm.getUpdateTime())
                .durationText(calcDuration(alarm.getAlarmTime()))
                .build());
        List<String> tags = new ArrayList<>();
        tags.add(AlarmTypeEnum.getLabel(alarm.getAlarmType()));
        if (alarm.getThresholdValue() != null) tags.add("阈值:" + alarm.getThresholdValue());
        vo.setTags(tags);
        return vo;
    }

    public static UnifiedListItemVO fromEvent(EventIncident e) {
        if (e == null) return null;
        UnifiedListItemVO vo = UnifiedListItemVO.builder().build();
        vo.setResourceType(15);
        vo.setResourceTypeName("事件");
        vo.setId(e.getId());
        vo.setCode(e.getEventCode());
        vo.setTitle(e.getTitle());
        vo.setPipelineType(e.getPipelineType());
        vo.setPipelineTypeName(PipelineTypeEnum.getLabel(e.getPipelineType()));
        vo.setPriority(e.getEventLevel());
        vo.setPriorityTag(eventPriorityTag(e.getEventLevel()));
        vo.setStatusTag(eventStatusTag(e.getStatus()));
        vo.setLocation(LocationVO.builder()
                .lng(e.getLng())
                .lat(e.getLat())
                .areaCode(e.getAreaCode())
                .areaName(e.getAreaName())
                .affectRadius(e.getAffectRadius())
                .build());
        vo.setImpact(ImpactInfoVO.builder()
                .level(e.getEventLevel())
                .levelName(EventLevelEnum.getLabel(e.getEventLevel()))
                .levelColor(EventLevelEnum.getColor(e.getEventLevel()))
                .affectedUsers(e.getEstimatedAffectedUsers())
                .build());
        vo.setHandler(HandlerInfoVO.builder()
                .userName(e.getResponsiblePerson())
                .deptName(null)
                .build());
        vo.setTimeline(TimelineInfoVO.builder()
                .createTime(e.getDiscoverTime())
                .updateTime(e.getUpdateTime())
                .actualStartTime(e.getStartDisposalTime())
                .actualFinishTime(e.getFinishDisposalTime())
                .durationText(calcDuration(e.getDiscoverTime()))
                .build());
        vo.setSourceId(e.getId());
        return vo;
    }

    public static UnifiedListItemVO fromWorkOrder(WorkOrder wo) {
        if (wo == null) return null;
        UnifiedListItemVO vo = UnifiedListItemVO.builder().build();
        vo.setResourceType(9);
        vo.setResourceTypeName("维修工单");
        vo.setId(wo.getId());
        vo.setCode(wo.getOrderCode());
        vo.setTitle(wo.getTitle());
        vo.setPipelineType(wo.getPipelineType());
        vo.setPipelineTypeName(PipelineTypeEnum.getLabel(wo.getPipelineType()));
        vo.setPriority(wo.getUrgency());
        vo.setPriorityTag(priorityTag(wo.getUrgency()));
        vo.setStatusTag(workOrderStatusTag(wo.getStatus(), wo.getProgress()));
        vo.setLocation(LocationVO.builder()
                .lng(wo.getLng())
                .lat(wo.getLat())
                .areaCode(wo.getAreaCode())
                .areaName(wo.getAreaName())
                .address(wo.getLocation())
                .build());
        vo.setHandler(HandlerInfoVO.builder()
                .deptName(wo.getUndertakeDept())
                .userName(wo.getUndertaker())
                .phone(wo.getContactPhone())
                .build());
        vo.setTimeline(TimelineInfoVO.builder()
                .createTime(wo.getCreateOrderTime())
                .updateTime(wo.getUpdateTime())
                .deadline(wo.getExpectCompleteTime())
                .actualStartTime(wo.getActualStartTime())
                .actualFinishTime(wo.getActualCompleteTime())
                .durationText(calcDuration(wo.getCreateOrderTime()))
                .build());
        vo.setSourceType(wo.getOrderSource());
        vo.setSourceName(WorkOrderSourceEnum.getLabel(wo.getOrderSource()));
        vo.setSourceId(wo.getAlarmId() != null ? wo.getAlarmId() :
                (wo.getHazardId() != null ? wo.getHazardId() : wo.getDefectId()));
        return vo;
    }

    public static UnifiedListItemVO fromExcavation(ExcavationApplication ex) {
        if (ex == null) return null;
        UnifiedListItemVO vo = UnifiedListItemVO.builder().build();
        vo.setResourceType(7);
        vo.setResourceTypeName("开挖申请");
        vo.setId(ex.getId());
        vo.setCode(ex.getApplicationCode());
        vo.setTitle(ex.getProjectName());
        vo.setPipelineType(ex.getAffectPipelineType());
        vo.setPipelineTypeName(PipelineTypeEnum.getLabel(ex.getAffectPipelineType()));
        vo.setPriority(ex.getImpactLevel());
        vo.setPriorityTag(priorityTag(ex.getImpactLevel()));
        vo.setStatusTag(excavationStatusTag(ex.getApprovalStatus()));
        vo.setLocation(LocationVO.builder()
                .lng(ex.getLng())
                .lat(ex.getLat())
                .areaCode(ex.getAreaCode())
                .roadName(ex.getRoadName())
                .address(ex.getAddress())
                .build());
        vo.setImpact(ImpactInfoVO.builder()
                .level(ex.getImpactLevel())
                .levelName(PriorityLevelEnum.getLabel(ex.getImpactLevel()))
                .levelColor(PriorityLevelEnum.getColor(ex.getImpactLevel()))
                .build());
        vo.setTimeline(TimelineInfoVO.builder()
                .createTime(ex.getCreateTime())
                .updateTime(ex.getUpdateTime())
                .deadline(ex.getPlanEndTime())
                .build());
        return vo;
    }

    public static UnifiedListItemVO fromHazard(Hazard h) {
        if (h == null) return null;
        UnifiedListItemVO vo = UnifiedListItemVO.builder().build();
        vo.setResourceType(6);
        vo.setResourceTypeName("隐患点");
        vo.setId(h.getId());
        vo.setCode(h.getHazardCode());
        vo.setTitle(h.getHazardTypeDesc());
        vo.setPipelineType(h.getPipelineType());
        vo.setPipelineTypeName(PipelineTypeEnum.getLabel(h.getPipelineType()));
        vo.setPriority(h.getRiskLevel());
        vo.setPriorityTag(priorityTag(h.getRiskLevel()));
        vo.setStatusTag(hazardStatusTag(h.getStatus()));
        vo.setLocation(LocationVO.builder()
                .lng(h.getLng())
                .lat(h.getLat())
                .areaCode(h.getAreaCode())
                .build());
        vo.setTimeline(TimelineInfoVO.builder()
                .createTime(h.getDiscoverTime())
                .updateTime(h.getUpdateTime())
                .durationText(calcDuration(h.getDiscoverTime()))
                .build());
        return vo;
    }

    public static StatusTagVO priorityTag(Integer level) {
        return StatusTagVO.builder()
                .code(level)
                .key(PriorityLevelEnum.of(level) != null ? PriorityLevelEnum.of(level).getKey() : "UNKNOWN")
                .label(PriorityLevelEnum.getLabel(level))
                .color(PriorityLevelEnum.getColor(level))
                .build();
    }

    public static StatusTagVO eventPriorityTag(Integer level) {
        return StatusTagVO.builder()
                .code(level)
                .key(EventLevelEnum.of(level) != null ? "" : "EVENT_" + level)
                .label(EventLevelEnum.getLabel(level))
                .color(EventLevelEnum.getColor(level))
                .build();
    }

    public static StatusTagVO statusTag(Integer status, String type) {
        if ("alarm".equals(type)) {
            return StatusTagVO.builder()
                    .code(status)
                    .label(AlarmStatusEnum.getLabel(status))
                    .color(status == 1 || status == 2 ? "#ef4444" : "#22c55e")
                    .build();
        }
        return StatusTagVO.builder().code(status).label("未知").color("#94a3b8").build();
    }

    public static StatusTagVO workOrderStatusTag(Integer status, Integer progress) {
        String color = "#64748b";
        if (status == 1) color = "#eab308";
        else if (status == 2 || status == 3) color = "#3b82f6";
        else if (status == 4) color = "#a855f7";
        else if (status == 5) color = "#22c55e";
        else if (status == 6) color = "#94a3b8";
        else if (status == 7) color = "#ef4444";
        return StatusTagVO.builder()
                .code(status)
                .label(WorkOrderStatusEnum.getLabel(status))
                .color(color)
                .progress(progress != null ? progress : WorkOrderStatusEnum.getProgress(status))
                .build();
    }

    public static StatusTagVO eventStatusTag(Integer status) {
        String[] labels = {"", "待响应", "处置中", "已控制", "已处置", "已恢复", "已归档"};
        String color = "#64748b";
        if (status != null) {
            if (status == 1) color = "#ef4444";
            else if (status == 2 || status == 3) color = "#f59e0b";
            else if (status >= 4) color = "#22c55e";
        }
        String label = (status != null && status < labels.length) ? labels[status] : "未知";
        return StatusTagVO.builder().code(status).label(label).color(color).build();
    }

    public static StatusTagVO excavationStatusTag(Integer status) {
        String color = "#64748b";
        if (status != null) {
            if (status == 1 || status == 2) color = "#eab308";
            else if (status == 3) color = "#22c55e";
            else if (status == 4) color = "#ef4444";
            else if (status == 5) color = "#3b82f6";
            else if (status >= 6) color = "#22c55e";
        }
        return StatusTagVO.builder()
                .code(status)
                .label(ExcavationStatusEnum.getLabel(status))
                .color(color)
                .build();
    }

    public static StatusTagVO hazardStatusTag(Integer status) {
        String[] labels = {"待排查", "已确认", "整改中", "已销项"};
        String color = status != null && status < 3 ? "#f59e0b" : "#22c55e";
        String label = (status != null && status >= 0 && status < labels.length) ? labels[status] : "未知";
        return StatusTagVO.builder().code(status).label(label).color(color).build();
    }

    public static String calcDuration(String startTimeStr) {
        if (startTimeStr == null || startTimeStr.isEmpty()) return "";
        try {
            LocalDateTime start = LocalDateTime.parse(startTimeStr, DTF);
            long minutes = Duration.between(start, LocalDateTime.now()).toMinutes();
            if (minutes < 60) return minutes + "分钟";
            long hours = minutes / 60;
            long remainMin = minutes % 60;
            if (hours < 24) return hours + "小时" + (remainMin > 0 ? remainMin + "分" : "");
            long days = hours / 24;
            long remainHours = hours % 24;
            return days + "天" + (remainHours > 0 ? remainHours + "小时" : "");
        } catch (Exception e) {
            return "";
        }
    }

    public static <T> UnifiedListResult<T> buildUnifiedPage(PageResult<T> pr) {
        UnifiedListResult<T> r = new UnifiedListResult<>();
        r.setList(pr.getRecords());
        r.setTotal(pr.getTotal());
        r.setPageNum(pr.getCurrent());
        r.setPageSize(pr.getSize());
        r.setPages(pr.getPages());
        return r;
    }
}
