package com.digitaltwin.pipeline.service.common.impl;

import cn.hutool.core.util.StrUtil;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.dto.common.*;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.entity.situation.EventIncident;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;
import com.digitaltwin.pipeline.enums.*;
import com.digitaltwin.pipeline.mapper.construction.ExcavationApplicationMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.mapper.situation.EventIncidentMapper;
import com.digitaltwin.pipeline.mapper.trace.ChangeHistoryMapper;
import com.digitaltwin.pipeline.service.common.UnifiedDetailService;
import com.digitaltwin.pipeline.service.common.UnifiedVOBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnifiedDetailServiceImpl implements UnifiedDetailService {

    private final AlarmMapper alarmMapper;
    private final EventIncidentMapper incidentMapper;
    private final WorkOrderMapper workOrderMapper;
    private final ExcavationApplicationMapper excavationMapper;
    private final HazardMapper hazardMapper;
    private final ChangeHistoryMapper changeHistoryMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public UnifiedDetailVO getDetail(Integer resourceType, Long resourceId) {
        if (resourceType == null || resourceId == null) {
            throw new BusinessException("资源类型和ID不能为空");
        }

        return switch (resourceType) {
            case 5 -> buildAlarmDetail(alarmMapper.selectById(resourceId));
            case 15 -> buildEventDetail(incidentMapper.selectById(resourceId));
            case 9 -> buildWorkOrderDetail(workOrderMapper.selectById(resourceId));
            case 7 -> buildExcavationDetail(excavationMapper.selectById(resourceId));
            case 6 -> buildHazardDetail(hazardMapper.selectById(resourceId));
            default -> throw new BusinessException("不支持的资源类型: " + resourceType);
        };
    }

    @Override
    public List<UnifiedDetailVO> batchGetDetails(List<BatchQueryItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();

        Map<Integer, List<Long>> groupById = new HashMap<>();
        Map<Integer, List<String>> groupByCode = new HashMap<>();

        for (BatchQueryItem item : items) {
            if (item.getResourceId() != null) {
                groupById.computeIfAbsent(item.getResourceType(), k -> new ArrayList<>()).add(item.getResourceId());
            } else if (StrUtil.isNotBlank(item.getResourceCode())) {
                groupByCode.computeIfAbsent(item.getResourceType(), k -> new ArrayList<>()).add(item.getResourceCode());
            }
        }

        List<UnifiedDetailVO> result = new ArrayList<>();

        if (groupById.containsKey(5) || groupByCode.containsKey(5)) {
            List<Alarm> list = new ArrayList<>();
            if (groupById.containsKey(5)) list.addAll(alarmMapper.selectBatchIds(groupById.get(5)));
            for (Alarm a : list) result.add(buildAlarmDetail(a));
        }
        if (groupById.containsKey(15) || groupByCode.containsKey(15)) {
            List<EventIncident> list = new ArrayList<>();
            if (groupById.containsKey(15)) list.addAll(incidentMapper.selectBatchIds(groupById.get(15)));
            for (EventIncident e : list) result.add(buildEventDetail(e));
        }
        if (groupById.containsKey(9) || groupByCode.containsKey(9)) {
            List<WorkOrder> list = new ArrayList<>();
            if (groupById.containsKey(9)) list.addAll(workOrderMapper.selectBatchIds(groupById.get(9)));
            for (WorkOrder w : list) result.add(buildWorkOrderDetail(w));
        }
        if (groupById.containsKey(7) || groupByCode.containsKey(7)) {
            List<ExcavationApplication> list = new ArrayList<>();
            if (groupById.containsKey(7)) list.addAll(excavationMapper.selectBatchIds(groupById.get(7)));
            for (ExcavationApplication ex : list) result.add(buildExcavationDetail(ex));
        }
        if (groupById.containsKey(6) || groupByCode.containsKey(6)) {
            List<Hazard> list = new ArrayList<>();
            if (groupById.containsKey(6)) list.addAll(hazardMapper.selectBatchIds(groupById.get(6)));
            for (Hazard h : list) result.add(buildHazardDetail(h));
        }

        return result;
    }

    @Override
    public List<UnifiedTimelineItemVO> getTimeline(Integer resourceType, Long resourceId, Integer limit) {
        if (resourceType == null || resourceId == null) return Collections.emptyList();
        if (limit == null || limit <= 0) limit = 20;

        Integer businessType = mapResourceTypeToBusinessType(resourceType);

        List<ChangeHistory> histories = changeHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChangeHistory>()
                        .eq(businessType != null, ChangeHistory::getBusinessType, businessType)
                        .eq(ChangeHistory::getBusinessId, resourceId)
                        .orderByDesc(ChangeHistory::getOperateTime)
                        .last("LIMIT " + limit)
        );

        if (histories.isEmpty()) {
            return generateBusinessTimeline(resourceType, resourceId, limit);
        }

        List<UnifiedTimelineItemVO> result = new ArrayList<>();
        for (ChangeHistory h : histories) {
            UnifiedTimelineItemVO item = UnifiedTimelineItemVO.builder()
                    .id(h.getId())
                    .pointType(h.getChangeType() != null ? h.getChangeType() : 1)
                    .pointTypeName(mapChangeTypeToName(h.getChangeType()))
                    .title(h.getFieldName() != null ? h.getFieldName() + "变更" : "状态变更")
                    .description(buildTimelineDesc(h))
                    .occurTime(h.getOperateTime())
                    .operator(HandlerInfoVO.builder()
                            .userName(h.getOperator())
                            .deptName(h.getOperatorDept())
                            .build())
                    .importance(calcImportance(h.getChangeType()))
                    .build();
            result.add(item);
        }
        return result;
    }

    private UnifiedDetailVO buildAlarmDetail(Alarm alarm) {
        if (alarm == null) throw new BusinessException("告警不存在");
        UnifiedListItemVO base = UnifiedVOBuilder.fromAlarm(alarm);
        UnifiedDetailVO vo = copyListToDetail(base);

        UnifiedDetailVO.DetailExtraVO extra = UnifiedDetailVO.DetailExtraVO.builder()
                .description(alarm.getDescription())
                .value(alarm.getAlarmValue() != null ? new BigDecimal(alarm.getAlarmValue()) : null)
                .threshold(alarm.getThresholdValue() != null ? new BigDecimal(alarm.getThresholdValue()) : null)
                .unit(alarm.getAlarmType() != null && alarm.getAlarmType() == 1 ? "MPa" : "m")
                .deviceCode(alarm.getSensorCode())
                .contactPhone(alarm.getContactPhone())
                .build();
        vo.setExtra(extra);

        vo.setRelatedResources(buildAlarmRelated(alarm));
        vo.setTimelineItems(getTimeline(5, alarm.getId(), 10));

        return vo;
    }

    private UnifiedDetailVO buildEventDetail(EventIncident e) {
        if (e == null) throw new BusinessException("事件不存在");
        UnifiedListItemVO base = UnifiedVOBuilder.fromEvent(e);
        UnifiedDetailVO vo = copyListToDetail(base);

        UnifiedDetailVO.DetailExtraVO extra = UnifiedDetailVO.DetailExtraVO.builder()
                .description(e.getDescription())
                .currentStage(e.getCurrentStage())
                .progressDesc(e.getProgressDesc())
                .involvedDeptCount(e.getInvolvedDeptCount())
                .fieldPersonCount(e.getFieldPersonCount())
                .estimatedCost(e.getEstimatedCost())
                .build();
        vo.setExtra(extra);

        vo.setRelatedResources(buildEventRelated(e));
        vo.setTimelineItems(getTimeline(15, e.getId(), 15));

        return vo;
    }

    private UnifiedDetailVO buildWorkOrderDetail(WorkOrder wo) {
        if (wo == null) throw new BusinessException("工单不存在");
        UnifiedListItemVO base = UnifiedVOBuilder.fromWorkOrder(wo);
        UnifiedDetailVO vo = copyListToDetail(base);

        UnifiedDetailVO.DetailExtraVO extra = UnifiedDetailVO.DetailExtraVO.builder()
                .description(wo.getDescription())
                .defectDesc(wo.getDefectDescription())
                .currentStage(wo.getCurrentStage())
                .estimatedCost(wo.getEstimatedCost())
                .actualCost(wo.getActualCost())
                .contactPhone(wo.getContactPhone())
                .build();
        vo.setExtra(extra);

        vo.setRelatedResources(buildWorkOrderRelated(wo));
        vo.setTimelineItems(getTimeline(9, wo.getId(), 15));

        return vo;
    }

    private UnifiedDetailVO buildExcavationDetail(ExcavationApplication ex) {
        if (ex == null) throw new BusinessException("开挖申请不存在");
        UnifiedListItemVO base = UnifiedVOBuilder.fromExcavation(ex);
        UnifiedDetailVO vo = copyListToDetail(base);

        UnifiedDetailVO.DetailExtraVO extra = UnifiedDetailVO.DetailExtraVO.builder()
                .description(ex.getProjectDesc())
                .constructionUnit(ex.getConstructionUnit())
                .constructionLeader(ex.getConstructionLeader())
                .contactPhone(ex.getContactPhone())
                .estimatedCost(ex.getBudgetAmount())
                .build();
        vo.setExtra(extra);

        vo.setRelatedResources(buildExcavationRelated(ex));
        vo.setTimelineItems(getTimeline(7, ex.getId(), 10));

        return vo;
    }

    private UnifiedDetailVO buildHazardDetail(Hazard h) {
        if (h == null) throw new BusinessException("隐患不存在");
        UnifiedListItemVO base = UnifiedVOBuilder.fromHazard(h);
        UnifiedDetailVO vo = copyListToDetail(base);

        UnifiedDetailVO.DetailExtraVO extra = UnifiedDetailVO.DetailExtraVO.builder()
                .description(h.getDescription())
                .riskScore(h.getRiskScore())
                .hazardType(h.getHazardTypeDesc())
                .defectDesc(h.getHazardDesc())
                .build();
        vo.setExtra(extra);

        vo.setRelatedResources(Collections.emptyList());
        vo.setTimelineItems(getTimeline(6, h.getId(), 10));

        return vo;
    }

    private UnifiedDetailVO copyListToDetail(UnifiedListItemVO base) {
        return UnifiedDetailVO.builder()
                .resourceType(base.getResourceType())
                .resourceTypeName(base.getResourceTypeName())
                .id(base.getId())
                .code(base.getCode())
                .title(base.getTitle())
                .pipelineType(base.getPipelineType())
                .pipelineTypeName(base.getPipelineTypeName())
                .priority(base.getPriority())
                .priorityTag(base.getPriorityTag())
                .statusTag(base.getStatusTag())
                .location(base.getLocation())
                .impact(base.getImpact())
                .handler(base.getHandler())
                .timeline(base.getTimeline())
                .tags(base.getTags())
                .sourceId(base.getSourceId())
                .sourceType(base.getSourceType())
                .sourceName(base.getSourceName())
                .build();
    }

    private List<UnifiedDetailVO.RelatedResourceVO> buildAlarmRelated(Alarm alarm) {
        List<UnifiedDetailVO.RelatedResourceVO> list = new ArrayList<>();

        List<WorkOrder> wos = workOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkOrder>()
                        .eq(WorkOrder::getAlarmId, alarm.getId())
                        .orderByDesc(WorkOrder::getCreateOrderTime)
                        .last("LIMIT 5")
        );
        for (WorkOrder wo : wos) {
            list.add(UnifiedDetailVO.RelatedResourceVO.builder()
                    .resourceType(9)
                    .resourceTypeName("维修工单")
                    .resourceId(wo.getId())
                    .resourceCode(wo.getOrderCode())
                    .title(wo.getTitle())
                    .statusTag(UnifiedVOBuilder.workOrderStatusTag(wo.getStatus(), wo.getProgress()))
                    .priorityTag(UnifiedVOBuilder.priorityTag(wo.getUrgency()))
                    .relationDesc("告警触发工单")
                    .build());
        }
        return list;
    }

    private List<UnifiedDetailVO.RelatedResourceVO> buildEventRelated(EventIncident e) {
        List<UnifiedDetailVO.RelatedResourceVO> list = new ArrayList<>();
        List<Alarm> alarms = alarmMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Alarm>()
                        .eq(Alarm::getAreaCode, e.getAreaCode())
                        .ge(Alarm::getAlarmLevel, 2)
                        .orderByDesc(Alarm::getAlarmTime)
                        .last("LIMIT 3")
        );
        for (Alarm a : alarms) {
            list.add(UnifiedDetailVO.RelatedResourceVO.builder()
                    .resourceType(5)
                    .resourceTypeName("相关告警")
                    .resourceId(a.getId())
                    .resourceCode(a.getAlarmCode())
                    .title(a.getTitle())
                    .statusTag(UnifiedVOBuilder.statusTag(a.getStatus(), "alarm"))
                    .priorityTag(UnifiedVOBuilder.priorityTag(a.getAlarmLevel()))
                    .relationDesc("同区域告警")
                    .build());
        }

        List<WorkOrder> wos = workOrderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkOrder>()
                        .eq(WorkOrder::getAreaCode, e.getAreaCode())
                        .in(WorkOrder::getStatus, 1, 2, 3, 4)
                        .orderByDesc(WorkOrder::getCreateOrderTime)
                        .last("LIMIT 3")
        );
        for (WorkOrder wo : wos) {
            list.add(UnifiedDetailVO.RelatedResourceVO.builder()
                    .resourceType(9)
                    .resourceTypeName("处置工单")
                    .resourceId(wo.getId())
                    .resourceCode(wo.getOrderCode())
                    .title(wo.getTitle())
                    .statusTag(UnifiedVOBuilder.workOrderStatusTag(wo.getStatus(), wo.getProgress()))
                    .priorityTag(UnifiedVOBuilder.priorityTag(wo.getUrgency()))
                    .relationDesc("同区域处置")
                    .build());
        }
        return list;
    }

    private List<UnifiedDetailVO.RelatedResourceVO> buildWorkOrderRelated(WorkOrder wo) {
        List<UnifiedDetailVO.RelatedResourceVO> list = new ArrayList<>();
        if (wo.getAlarmId() != null) {
            Alarm a = alarmMapper.selectById(wo.getAlarmId());
            if (a != null) {
                list.add(UnifiedDetailVO.RelatedResourceVO.builder()
                        .resourceType(5)
                        .resourceTypeName("源告警")
                        .resourceId(a.getId())
                        .resourceCode(a.getAlarmCode())
                        .title(a.getTitle())
                        .statusTag(UnifiedVOBuilder.statusTag(a.getStatus(), "alarm"))
                        .relationDesc("工单来源")
                        .build());
            }
        }
        if (wo.getHazardId() != null) {
            Hazard h = hazardMapper.selectById(wo.getHazardId());
            if (h != null) {
                list.add(UnifiedDetailVO.RelatedResourceVO.builder()
                        .resourceType(6)
                        .resourceTypeName("源隐患")
                        .resourceId(h.getId())
                        .resourceCode(h.getHazardCode())
                        .title(h.getHazardTypeDesc())
                        .statusTag(UnifiedVOBuilder.hazardStatusTag(h.getStatus()))
                        .relationDesc("工单来源")
                        .build());
            }
        }
        return list;
    }

    private List<UnifiedDetailVO.RelatedResourceVO> buildExcavationRelated(ExcavationApplication ex) {
        List<UnifiedDetailVO.RelatedResourceVO> list = new ArrayList<>();

        List<Alarm> alarms = alarmMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Alarm>()
                        .eq(Alarm::getAreaCode, ex.getAreaCode())
                        .orderByDesc(Alarm::getAlarmTime)
                        .last("LIMIT 3")
        );
        for (Alarm a : alarms) {
            list.add(UnifiedDetailVO.RelatedResourceVO.builder()
                    .resourceType(5)
                    .resourceTypeName("附近告警")
                    .resourceId(a.getId())
                    .resourceCode(a.getAlarmCode())
                    .title(a.getTitle())
                    .statusTag(UnifiedVOBuilder.statusTag(a.getStatus(), "alarm"))
                    .relationDesc("同区域")
                    .build());
        }
        return list;
    }

    private List<UnifiedTimelineItemVO> generateBusinessTimeline(Integer resourceType, Long resourceId, Integer limit) {
        List<UnifiedTimelineItemVO> list = new ArrayList<>();
        String baseTime = LocalDateTime.now().format(DTF);
        String[] pointNames = {"发现", "派单", "到达", "处置中", "待验收", "已完成"};
        for (int i = 0; i < Math.min(limit, 6); i++) {
            LocalDateTime t = safeParse(baseTime).minusMinutes((5 - i) * 30L + new Random().nextInt(20));
            UnifiedTimelineItemVO item = UnifiedTimelineItemVO.builder()
                    .id((long) (i + 1))
                    .pointType(i + 1)
                    .pointTypeName(pointNames[i])
                    .title(pointNames[i])
                    .description(describePoint(i, resourceType))
                    .occurTime(t.format(DTF))
                    .operator(HandlerInfoVO.builder().userName("系统自动").build())
                    .importance(4 - i / 2)
                    .build();
            list.add(item);
        }
        return list;
    }

    private String describePoint(int index, Integer resourceType) {
        String typeName = ResourceTypeEnum.getLabel(resourceType);
        return switch (index) {
            case 0 -> typeName + "首次发现";
            case 1 -> "系统自动派单至责任部门";
            case 2 -> "现场人员到达处置位置";
            case 3 -> "正在按方案进行处置";
            case 4 -> "处置完成，待验收";
            case 5 -> "验收通过，已归档";
            default -> "状态变更";
        };
    }

    private String buildTimelineDesc(ChangeHistory h) {
        StringBuilder sb = new StringBuilder();
        if (h.getFieldName() != null) sb.append(h.getFieldName());
        if (h.getOldValue() != null && h.getNewValue() != null) {
            sb.append("：").append(h.getOldValue()).append(" → ").append(h.getNewValue());
        }
        if (h.getDescription() != null) sb.append("（").append(h.getDescription()).append("）");
        return sb.length() > 0 ? sb.toString() : "内容变更";
    }

    private int calcImportance(Integer changeType) {
        if (changeType == null) return 2;
        return switch (changeType) {
            case 3 -> 4;
            case 4, 5 -> 3;
            case 1, 2 -> 2;
            default -> 2;
        };
    }

    private Integer mapResourceTypeToBusinessType(Integer resourceType) {
        if (resourceType == null) return null;
        return switch (resourceType) {
            case 5 -> 4;
            case 6 -> 5;
            case 7 -> 7;
            case 9 -> 6;
            default -> null;
        };
    }

    private String mapChangeTypeToName(Integer changeType) {
        if (changeType == null) return "变更";
        return switch (changeType) {
            case 1 -> "新增";
            case 2 -> "修改";
            case 3 -> "删除";
            case 4 -> "状态变更";
            case 5 -> "流程流转";
            default -> "变更";
        };
    }

    private LocalDateTime safeParse(String s) {
        if (s == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(s, DTF);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
