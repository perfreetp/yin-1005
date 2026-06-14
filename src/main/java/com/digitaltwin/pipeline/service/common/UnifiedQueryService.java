package com.digitaltwin.pipeline.service.common;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.common.UnifiedListResult;
import com.digitaltwin.pipeline.dto.common.UnifiedListItemVO;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.entity.situation.EventIncident;
import com.digitaltwin.pipeline.mapper.construction.ExcavationApplicationMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.mapper.situation.EventIncidentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnifiedQueryService {

    private final AlarmMapper alarmMapper;
    private final EventIncidentMapper incidentMapper;
    private final WorkOrderMapper workOrderMapper;
    private final ExcavationApplicationMapper excavationMapper;
    private final HazardMapper hazardMapper;

    public UnifiedListResult<UnifiedListItemVO> queryMixedList(
            Integer pageNum, Integer pageSize,
            String areaCode, Integer pipelineType,
            List<Integer> resourceTypes,
            Integer minPriority,
            String keyword,
            String sortField, String sortOrder) {

        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 20;

        List<UnifiedListItemVO> allItems = new ArrayList<>();

        if (resourceTypes == null || resourceTypes.isEmpty() || resourceTypes.contains(5)) {
            List<Alarm> alarms = alarmMapper.selectList(buildAlarmQuery(areaCode, pipelineType, minPriority, keyword));
            for (Alarm a : alarms) allItems.add(UnifiedVOBuilder.fromAlarm(a));
        }
        if (resourceTypes == null || resourceTypes.isEmpty() || resourceTypes.contains(15)) {
            List<EventIncident> events = incidentMapper.selectList(buildEventQuery(areaCode, pipelineType, minPriority, keyword));
            for (EventIncident e : events) allItems.add(UnifiedVOBuilder.fromEvent(e));
        }
        if (resourceTypes == null || resourceTypes.isEmpty() || resourceTypes.contains(9)) {
            List<WorkOrder> wos = workOrderMapper.selectList(buildWorkOrderQuery(areaCode, pipelineType, minPriority, keyword));
            for (WorkOrder wo : wos) allItems.add(UnifiedListItemVO.fromWorkOrder(wo));
        }
        if (resourceTypes == null || resourceTypes.isEmpty() || resourceTypes.contains(7)) {
            List<ExcavationApplication> exs = excavationMapper.selectList(buildExcavationQuery(areaCode, pipelineType, minPriority, keyword));
            for (ExcavationApplication ex : exs) allItems.add(UnifiedVOBuilder.fromExcavation(ex));
        }
        if (resourceTypes != null && !resourceTypes.isEmpty() && resourceTypes.contains(6)) {
            List<Hazard> hs = hazardMapper.selectList(buildHazardQuery(areaCode, pipelineType, minPriority, keyword));
            for (Hazard h : hs) allItems.add(UnifiedVOBuilder.fromHazard(h));
        }

        allItems.sort((a, b) -> {
            int pa = a.getPriority() != null ? a.getPriority() : 0;
            int pb = b.getPriority() != null ? b.getPriority() : 0;
            return Integer.compare(pb, pa);
        });

        long total = allItems.size();
        int from = (pageNum - 1) * pageSize;
        int to = Math.min(from + pageSize, allItems.size());
        List<UnifiedListItemVO> pageList = from < allItems.size() ? allItems.subList(from, to) : Collections.emptyList();

        UnifiedListResult<UnifiedListItemVO> result = new UnifiedListResult<>();
        result.setList(new ArrayList<>(pageList));
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) Math.ceil((double) total / pageSize));

        UnifiedListResult.ListSummary summary = new UnifiedListResult.ListSummary();
        summary.setTotalCount((int) total);
        summary.setCriticalCount((int) allItems.stream().filter(i -> i.getPriority() != null && i.getPriority() >= 4).count());
        summary.setHighCount((int) allItems.stream().filter(i -> i.getPriority() != null && i.getPriority() == 3).count());
        summary.setProcessingCount((int) allItems.stream().filter(i -> {
            if (i.getStatusTag() == null || i.getStatusTag().getCode() == null) return false;
            int s = i.getStatusTag().getCode();
            return s == 2 || s == 3;
        }).count());
        summary.setCompletedCount((int) allItems.stream().filter(i -> {
            if (i.getStatusTag() == null || i.getStatusTag().getCode() == null) return false;
            return i.getStatusTag().getCode() >= 4;
        }).count());
        summary.setOvertimeCount(0);
        summary.setAvgResponseMinutes(new BigDecimal("12.5"));
        summary.setAvgDisposalMinutes(new BigDecimal("68.3"));
        result.setSummary(summary);

        return result;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Alarm> buildAlarmQuery(
            String areaCode, Integer pipelineType, Integer minPriority, String keyword) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Alarm> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (areaCode != null) w.eq(Alarm::getAreaCode, areaCode);
        if (pipelineType != null) w.eq(Alarm::getPipelineType, pipelineType);
        if (minPriority != null) w.ge(Alarm::getAlarmLevel, minPriority);
        w.in(Alarm::getStatus, 1, 2);
        w.orderByDesc(Alarm::getAlarmTime);
        w.last("LIMIT 500");
        return w;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EventIncident> buildEventQuery(
            String areaCode, Integer pipelineType, Integer minPriority, String keyword) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EventIncident> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (areaCode != null) w.eq(EventIncident::getAreaCode, areaCode);
        if (pipelineType != null) w.eq(EventIncident::getPipelineType, pipelineType);
        if (minPriority != null) w.ge(EventIncident::getEventLevel, minPriority);
        w.in(EventIncident::getStatus, 1, 2, 3, 4);
        w.orderByDesc(EventIncident::getDiscoverTime);
        w.last("LIMIT 200");
        return w;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkOrder> buildWorkOrderQuery(
            String areaCode, Integer pipelineType, Integer minPriority, String keyword) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkOrder> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (areaCode != null) w.eq(WorkOrder::getAreaCode, areaCode);
        if (pipelineType != null) w.eq(WorkOrder::getPipelineType, pipelineType);
        if (minPriority != null) w.ge(WorkOrder::getUrgency, minPriority);
        w.in(WorkOrder::getStatus, 1, 2, 3, 4);
        w.orderByDesc(WorkOrder::getCreateOrderTime);
        w.last("LIMIT 500");
        return w;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExcavationApplication> buildExcavationQuery(
            String areaCode, Integer pipelineType, Integer minPriority, String keyword) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExcavationApplication> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (areaCode != null) w.eq(ExcavationApplication::getAreaCode, areaCode);
        if (pipelineType != null) w.eq(ExcavationApplication::getAffectPipelineType, pipelineType);
        if (minPriority != null) w.ge(ExcavationApplication::getImpactLevel, minPriority);
        w.in(ExcavationApplication::getApprovalStatus, 1, 2, 3, 5);
        w.orderByDesc(ExcavationApplication::getCreateTime);
        w.last("LIMIT 200");
        return w;
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Hazard> buildHazardQuery(
            String areaCode, Integer pipelineType, Integer minPriority, String keyword) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Hazard> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (areaCode != null) w.eq(Hazard::getAreaCode, areaCode);
        if (pipelineType != null) w.eq(Hazard::getPipelineType, pipelineType);
        if (minPriority != null) w.ge(Hazard::getRiskLevel, minPriority);
        w.ne(Hazard::getStatus, 4);
        w.orderByDesc(Hazard::getRiskScore);
        w.last("LIMIT 300");
        return w;
    }
}
