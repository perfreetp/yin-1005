package com.digitaltwin.pipeline.service.sensor.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.dto.sensor.AlarmDisposalSuggestionDTO;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.entity.auth.SysDepartment;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import com.digitaltwin.pipeline.entity.sensor.SensorReading;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.mapper.auth.SysDepartmentMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.mapper.sensor.SensorMapper;
import com.digitaltwin.pipeline.mapper.sensor.SensorReadingMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.service.sensor.AlarmDisposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlarmDisposalServiceImpl implements AlarmDisposalService {

    private final AlarmMapper alarmMapper;
    private final SensorMapper sensorMapper;
    private final SensorReadingMapper readingMapper;
    private final PipelineMapper pipelineMapper;
    private final ValveMapper valveMapper;
    private final HazardMapper hazardMapper;
    private final WorkOrderMapper workOrderMapper;
    private final SysDepartmentMapper departmentMapper;

    private static final String[] ALARM_TYPE_NAMES = {"", "压力超限", "液位超限", "流量异常", "温度异常",
            "振动异常", "气体泄漏", "水质异常", "设备离线", "其他"};
    private static final String[] PIPELINE_TYPE_NAMES = {"", "给水", "排水", "燃气", "电力", "通信", "热力", "工业"};
    private static final String[] VALVE_TYPE_NAMES = {"", "闸阀", "蝶阀", "球阀", "截止阀", "止回阀"};
    private static final String[] PRIORITY_NAMES = {"", "常规处置", "关注处置", "紧急处置", "特级处置"};
    private static final String[] TREND_NAMES = {"", "正在恶化", "持续稳定", "正在缓解", "已恢复正常"};
    private static final String[] IMPACT_LEVEL_NAMES = {"", "局部影响", "片区影响", "区域影响", "大范围影响"};

    @Override
    public AlarmDisposalSuggestionDTO getDisposalSuggestion(Long alarmId) {
        Alarm alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }

        AlarmDisposalSuggestionDTO result = new AlarmDisposalSuggestionDTO();
        result.setAlarmId(alarm.getId());
        result.setAlarmCode(alarm.getAlarmCode());
        result.setTitle(alarm.getTitle());
        result.setAlarmType(alarm.getAlarmType());
        result.setAlarmTypeName(alarm.getAlarmType() != null && alarm.getAlarmType() < ALARM_TYPE_NAMES.length
                ? ALARM_TYPE_NAMES[alarm.getAlarmType()] : "未知");
        result.setOriginalLevel(alarm.getAlarmLevel());

        Sensor sensor = null;
        Pipeline pipeline = null;
        if (alarm.getSensorId() != null) {
            sensor = sensorMapper.selectById(alarm.getSensorId());
        }
        if (alarm.getPipelineId() != null) {
            pipeline = pipelineMapper.selectById(alarm.getPipelineId());
        }

        List<AlarmDisposalSuggestionDTO.ReadingPoint> recentReadings = buildRecentReadings(alarm);
        result.setRecentReadings(recentReadings);

        int trendStatus = analyzeTrend(recentReadings, alarm);
        result.setTrendStatus(trendStatus);
        result.setTrendStatusName(TREND_NAMES[trendStatus]);

        int priority = calculateDisposalPriority(alarm, trendStatus, pipeline);
        result.setDisposalPriority(priority);
        result.setDisposalPriorityName(PRIORITY_NAMES[priority]);
        result.setSuggestedResponseMinutes(getResponseMinutes(priority));
        result.setSuggestedArrivalMinutes(getArrivalMinutes(priority));

        result.setDisposalSteps(buildDisposalSteps(alarm, priority, pipeline));

        result.setSuggestedValveClosures(buildValveClosureOrders(alarm, pipeline));

        result.setAffectedArea(buildAffectedArea(alarm, pipeline));

        result.setRelatedHazard(buildRelatedHazardInfo(alarm));

        result.setRelatedWorkOrder(buildRelatedWorkOrderInfo(alarm));

        result.setHistoryStats(buildHistoryAlarmStats(alarm));

        result.setOperatorReminder(generateOperatorReminder(result, pipeline));

        fillSuggestedHandler(result, pipeline);

        return result;
    }

    @Override
    public List<AlarmDisposalSuggestionDTO> getPendingAlarmPriorityList() {
        List<Alarm> pendingAlarms = alarmMapper.selectList(
                new LambdaQueryWrapper<Alarm>()
                        .in(Alarm::getStatus, 1, 2)
                        .orderByDesc(Alarm::getAlarmLevel, Alarm::getAlarmTime));

        List<AlarmDisposalSuggestionDTO> result = new ArrayList<>();
        for (Alarm alarm : pendingAlarms) {
            try {
                AlarmDisposalSuggestionDTO suggestion = getDisposalSuggestion(alarm.getId());
                result.add(suggestion);
            } catch (Exception ignored) {
            }
        }

        result.sort(Comparator
                .comparingInt(AlarmDisposalSuggestionDTO::getDisposalPriority).reversed()
                .thenComparing(Comparator.comparingInt(AlarmDisposalSuggestionDTO::getTrendStatus))
                .thenComparing(Comparator.comparing(AlarmDisposalSuggestionDTO::getAlarmId)));

        return result;
    }

    private List<AlarmDisposalSuggestionDTO.ReadingPoint> buildRecentReadings(Alarm alarm) {
        List<AlarmDisposalSuggestionDTO.ReadingPoint> result = new ArrayList<>();
        if (alarm.getSensorId() == null) return result;

        LocalDateTime now = LocalDateTime.now();
        String oneHourAgo = now.minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<SensorReading> readings = readingMapper.selectList(
                new LambdaQueryWrapper<SensorReading>()
                        .eq(SensorReading::getSensorId, alarm.getSensorId())
                        .ge(SensorReading::getCollectTime, oneHourAgo)
                        .orderByAsc(SensorReading::getCollectTime)
                        .last("LIMIT 60"));

        BigDecimal threshold = alarm.getThresholdValue();
        for (SensorReading r : readings) {
            AlarmDisposalSuggestionDTO.ReadingPoint p = new AlarmDisposalSuggestionDTO.ReadingPoint();
            p.setTime(r.getCollectTime());
            p.setValue(r.getReadingValue());
            if (threshold != null && r.getReadingValue() != null) {
                p.setOutOfRange(r.getReadingValue().compareTo(threshold) >= 0);
            } else {
                p.setOutOfRange(r.getIsAlarm() != null && r.getIsAlarm() == 1);
            }
            result.add(p);
        }
        return result;
    }

    private int analyzeTrend(List<AlarmDisposalSuggestionDTO.ReadingPoint> readings, Alarm alarm) {
        if (readings.size() < 3) return 2;

        int n = readings.size();
        List<BigDecimal> values = readings.stream()
                .map(AlarmDisposalSuggestionDTO.ReadingPoint::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (values.size() < 3) return 2;

        int firstHalf = values.size() / 2;
        BigDecimal avg1 = average(values.subList(0, firstHalf));
        BigDecimal avg2 = average(values.subList(firstHalf, values.size()));

        if (avg1 == null || avg2 == null) return 2;

        BigDecimal change = avg2.subtract(avg1);
        BigDecimal threshold2 = alarm.getThresholdValue();
        if (threshold2 != null && threshold2.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = change.divide(threshold2, 4, RoundingMode.HALF_UP).abs();
            BigDecimal lastValue = values.get(values.size() - 1);
            boolean stillOver = lastValue.compareTo(threshold2) >= 0;

            if (change.compareTo(BigDecimal.ZERO) > 0 && ratio.compareTo(new BigDecimal("0.05")) > 0) return 1;
            if (!stillOver) return 4;
            if (change.compareTo(BigDecimal.ZERO) < 0 && ratio.compareTo(new BigDecimal("0.05")) > 0) return 3;
            return 2;
        }

        if (change.compareTo(new BigDecimal("0.1")) > 0) return 1;
        if (change.compareTo(new BigDecimal("-0.1")) < 0) return 3;
        return 2;
    }

    private int calculateDisposalPriority(Alarm alarm, int trendStatus, Pipeline pipeline) {
        int priority = alarm.getAlarmLevel() != null ? alarm.getAlarmLevel() : 1;

        if (trendStatus == 1) priority = Math.min(priority + 1, 4);
        else if (trendStatus == 4) priority = Math.max(priority - 1, 1);

        if (pipeline != null && pipeline.getPipelineType() != null) {
            if (pipeline.getPipelineType() == 3) priority = Math.min(priority + 1, 4);
            if (pipeline.getPipelineType() == 4) priority = Math.min(priority + 1, 4);
        }

        if (alarm.getAlarmType() != null) {
            if (alarm.getAlarmType() == 6) priority = Math.min(priority + 1, 4);
            if (alarm.getAlarmType() == 1 && pipeline != null
                    && pipeline.getPressureLevel() != null && pipeline.getPressureLevel() >= 3) {
                priority = Math.min(priority + 1, 4);
            }
        }

        return priority;
    }

    private int getResponseMinutes(int priority) {
        return switch (priority) {
            case 4 -> 5;
            case 3 -> 15;
            case 2 -> 30;
            default -> 60;
        };
    }

    private int getArrivalMinutes(int priority) {
        return switch (priority) {
            case 4 -> 15;
            case 3 -> 30;
            case 2 -> 60;
            default -> 120;
        };
    }

    private List<AlarmDisposalSuggestionDTO.DisposalStep> buildDisposalSteps(
            Alarm alarm, int priority, Pipeline pipeline) {
        List<AlarmDisposalSuggestionDTO.DisposalStep> steps = new ArrayList<>();

        AlarmDisposalSuggestionDTO.DisposalStep s1 = new AlarmDisposalSuggestionDTO.DisposalStep();
        s1.setStepNo(1);
        s1.setStepName("确认告警");
        s1.setInstruction("查看实时监测数据，确认告警是否真实，排除传感器误报");
        s1.setRequired(1);
        s1.setEstimatedMinutes(5);
        s1.setCaution("请勿直接忽略告警，即使怀疑误报也需现场确认");
        steps.add(s1);

        AlarmDisposalSuggestionDTO.DisposalStep s2 = new AlarmDisposalSuggestionDTO.DisposalStep();
        s2.setStepNo(2);
        s2.setStepName("通知相关方");
        s2.setInstruction(priority >= 3 ? "立即电话通知运维负责人、产权单位，必要时通知消防/应急部门"
                : "通知当值运维人员，告知告警详情和位置");
        s2.setRequired(1);
        s2.setEstimatedMinutes(priority >= 3 ? 3 : 10);
        s2.setCaution(priority >= 3 ? "燃气/压力告警优先通知，保持通话畅通" : "记录通知内容和时间");
        steps.add(s2);

        if (priority >= 3 && pipeline != null && (pipeline.getPipelineType() == 1 || pipeline.getPipelineType() == 3)) {
            AlarmDisposalSuggestionDTO.DisposalStep s3 = new AlarmDisposalSuggestionDTO.DisposalStep();
            s3.setStepNo(3);
            s3.setStepName("关阀隔离");
            s3.setInstruction("按关阀顺序表操作，先下游后上游，逐步隔离故障段");
            s3.setRequired(1);
            s3.setEstimatedMinutes(20);
            s3.setCaution(pipeline.getPipelineType() == 3 ? "燃气管道关阀后需检测可燃气体浓度，禁止明火" : "关阀后需确认压力下降正常");
            steps.add(s3);
        }

        AlarmDisposalSuggestionDTO.DisposalStep s4 = new AlarmDisposalSuggestionDTO.DisposalStep();
        s4.setStepNo(steps.size() + 1);
        s4.setStepName("现场排查");
        s4.setInstruction("抵达告警位置，检查管线外观、接口、井室情况，定位故障点");
        s4.setRequired(1);
        s4.setEstimatedMinutes(30);
        s4.setCaution("下井作业前需通风、检测气体浓度，系好安全绳");
        steps.add(s4);

        AlarmDisposalSuggestionDTO.DisposalStep s5 = new AlarmDisposalSuggestionDTO.DisposalStep();
        s5.setStepNo(steps.size() + 1);
        s5.setStepName("故障处置");
        s5.setInstruction("根据故障类型执行维修/更换/堵漏，重大故障需升级汇报");
        s5.setRequired(1);
        s5.setEstimatedMinutes(60);
        s5.setCaution("维修过程全程拍照记录，关键节点需两人以上确认");
        steps.add(s5);

        AlarmDisposalSuggestionDTO.DisposalStep s6 = new AlarmDisposalSuggestionDTO.DisposalStep();
        s6.setStepNo(steps.size() + 1);
        s6.setStepName("恢复验证");
        s6.setInstruction("修复后开启阀门，监测压力/液位/流量恢复正常，持续观察30分钟");
        s6.setRequired(1);
        s6.setEstimatedMinutes(45);
        s6.setCaution("缓慢升压，避免水锤/气锤效应");
        steps.add(s6);

        AlarmDisposalSuggestionDTO.DisposalStep s7 = new AlarmDisposalSuggestionDTO.DisposalStep();
        s7.setStepNo(steps.size() + 1);
        s7.setStepName("记录归档");
        s7.setInstruction("填写处置记录，上传现场照片，创建或关闭关联工单");
        s7.setRequired(1);
        s7.setEstimatedMinutes(15);
        s7.setCaution("若为重复告警，需标注并安排专项排查");
        steps.add(s7);

        return steps;
    }

    private List<AlarmDisposalSuggestionDTO.ValveClosingOrder> buildValveClosureOrders(Alarm alarm, Pipeline pipeline) {
        List<AlarmDisposalSuggestionDTO.ValveClosingOrder> result = new ArrayList<>();

        if (pipeline == null || pipeline.getId() == null) return result;
        if (pipeline.getPipelineType() != null && !Arrays.asList(1, 3, 6).contains(pipeline.getPipelineType())) {
            return result;
        }

        List<Valve> valves = valveMapper.selectByPipelineId(pipeline.getId());
        if (valves.isEmpty()) return result;

        valves.sort((v1, v2) -> {
            double d1 = calculateDistance(v1.getLng(), v1.getLat(), alarm.getLng(), alarm.getLat());
            double d2 = calculateDistance(v2.getLng(), v2.getLat(), alarm.getLng(), alarm.getLat());
            return Double.compare(d1, d2);
        });

        int orderNo = 1;
        for (Valve valve : valves) {
            if (orderNo > 5) break;

            AlarmDisposalSuggestionDTO.ValveClosingOrder order = new AlarmDisposalSuggestionDTO.ValveClosingOrder();
            order.setOrderNo(orderNo++);
            order.setValveId(valve.getId());
            order.setValveCode(valve.getValveCode());
            order.setValveName(valve.getValveName());
            order.setValveType(valve.getValveType() != null && valve.getValveType() < VALVE_TYPE_NAMES.length
                    ? VALVE_TYPE_NAMES[valve.getValveType()] : "未知");
            order.setDiameter(valve.getDiameter());
            order.setLng(valve.getLng());
            order.setLat(valve.getLat());
            double dist = calculateDistance(valve.getLng(), valve.getLat(), alarm.getLng(), alarm.getLat());
            order.setDistance(BigDecimal.valueOf(dist).setScale(1, RoundingMode.HALF_UP));
            order.setRemoteControllable(valve.getRemoteControllable());
            order.setEstimatedAffectedUsers(50 + new Random().nextInt(500));

            String pTypeName = pipeline.getPipelineType() != null && pipeline.getPipelineType() < PIPELINE_TYPE_NAMES.length
                    ? PIPELINE_TYPE_NAMES[pipeline.getPipelineType()] : "";
            order.setDescription(String.format("%s第%d道隔离阀，距告警点%.0f米，%s优先关闭",
                    pTypeName, orderNo - 1, dist,
                    order.getRemoteControllable() != null && order.getRemoteControllable() == 1 ? "可远程" : "需现场"));

            result.add(order);
        }

        return result;
    }

    private AlarmDisposalSuggestionDTO.AffectedAreaDTO buildAffectedArea(Alarm alarm, Pipeline pipeline) {
        AlarmDisposalSuggestionDTO.AffectedAreaDTO area = new AlarmDisposalSuggestionDTO.AffectedAreaDTO();
        area.setCenterLng(alarm.getLng());
        area.setCenterLat(alarm.getLat());

        int impactLevel = 1;
        double radius = 50;

        if (alarm.getAlarmLevel() != null) {
            switch (alarm.getAlarmLevel()) {
                case 4 -> { impactLevel = 4; radius = 500; }
                case 3 -> { impactLevel = 3; radius = 300; }
                case 2 -> { impactLevel = 2; radius = 150; }
                default -> { impactLevel = 1; radius = 50; }
            }
        }

        if (pipeline != null) {
            if (pipeline.getPipelineType() != null && (pipeline.getPipelineType() == 3 || pipeline.getPipelineType() == 4)) {
                impactLevel = Math.min(impactLevel + 1, 4);
                radius *= 1.5;
            }
            if (pipeline.getDiameter() != null && pipeline.getDiameter().compareTo(new BigDecimal("500")) > 0) {
                impactLevel = Math.min(impactLevel + 1, 4);
                radius *= 1.3;
            }
        }

        area.setImpactLevel(impactLevel);
        area.setImpactLevelName(IMPACT_LEVEL_NAMES[impactLevel]);
        area.setRadius(BigDecimal.valueOf(radius).setScale(0, RoundingMode.HALF_UP));

        String roadName = pipeline != null ? pipeline.getRoadName() : null;
        List<String> roads = new ArrayList<>();
        if (StrUtil.isNotBlank(roadName)) roads.add(roadName);
        if (impactLevel >= 2) roads.add("周边支路");
        if (impactLevel >= 3) roads.add("相邻主干道");
        area.setAffectedRoads(roads);

        List<String> buildings = new ArrayList<>();
        if (impactLevel >= 1) buildings.add("附近建筑");
        if (impactLevel >= 2) buildings.add("沿线小区");
        if (impactLevel >= 3) buildings.add("周边企事业单位");
        if (impactLevel >= 4) buildings.add("整片区域用户");
        area.setAffectedBuildings(buildings);

        int baseUsers = impactLevel * 200;
        if (pipeline != null && pipeline.getPipelineType() != null) {
            if (pipeline.getPipelineType() == 1) baseUsers *= 2;
            if (pipeline.getPipelineType() == 4) baseUsers *= 3;
        }
        area.setEstimatedAffectedUsers(baseUsers + new Random().nextInt(baseUsers));
        area.setEstimatedAffectedEnterprises(impactLevel * 10 + new Random().nextInt(20));

        List<String> parties = new ArrayList<>();
        if (impactLevel >= 2) parties.add("小区物业");
        if (impactLevel >= 3) parties.add("社区居委会");
        if (impactLevel >= 4) parties.add("街道办");
        if (pipeline != null && pipeline.getPipelineType() != null && pipeline.getPipelineType() == 3 && impactLevel >= 2) {
            parties.add("消防部门");
            parties.add("应急管理局");
        }
        area.setNeedNotifyParties(parties);

        return area;
    }

    private AlarmDisposalSuggestionDTO.RelatedHazardInfo buildRelatedHazardInfo(Alarm alarm) {
        AlarmDisposalSuggestionDTO.RelatedHazardInfo info = new AlarmDisposalSuggestionDTO.RelatedHazardInfo();
        info.setExists(0);

        if (alarm.getHazardId() != null) {
            Hazard hazard = hazardMapper.selectById(alarm.getHazardId());
            if (hazard != null) {
                info.setExists(1);
                info.setHazardId(hazard.getId());
                info.setHazardCode(hazard.getHazardCode());
                info.setRiskLevel(hazard.getRiskLevel());
                info.setRiskScore(hazard.getRiskScore());
                info.setStatus(hazard.getStatus());
                if (hazard.getDiscoverTime() != null) {
                    try {
                        LocalDateTime discover = LocalDateTime.parse(hazard.getDiscoverTime(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        info.setDaysSinceDiscovered((int) ChronoUnit.DAYS.between(discover, LocalDateTime.now()));
                    } catch (Exception ignored) {}
                }
            }
        }
        return info;
    }

    private AlarmDisposalSuggestionDTO.RelatedWorkOrderInfo buildRelatedWorkOrderInfo(Alarm alarm) {
        AlarmDisposalSuggestionDTO.RelatedWorkOrderInfo info = new AlarmDisposalSuggestionDTO.RelatedWorkOrderInfo();
        info.setExists(0);

        WorkOrder wo = null;
        if (alarm.getWorkOrderId() != null) {
            wo = workOrderMapper.selectById(alarm.getWorkOrderId());
        } else {
            List<WorkOrder> list = workOrderMapper.selectList(
                    new LambdaQueryWrapper<WorkOrder>()
                            .eq(WorkOrder::getAlarmId, alarm.getId())
                            .notIn(WorkOrder::getStatus, 5, 6)
                            .last("LIMIT 1"));
            if (!list.isEmpty()) wo = list.get(0);
        }

        if (wo != null) {
            info.setExists(1);
            info.setWorkOrderId(wo.getId());
            info.setOrderCode(wo.getOrderCode());
            info.setUrgency(wo.getUrgency());
            info.setStatus(wo.getStatus());
            info.setCurrentNode(wo.getCurrentNode());
            if (wo.getCreateOrderTime() != null) {
                try {
                    LocalDateTime created = LocalDateTime.parse(wo.getCreateOrderTime(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    long minutes = ChronoUnit.MINUTES.between(created, LocalDateTime.now());
                    info.setHoursSinceCreated(BigDecimal.valueOf(minutes / 60.0).setScale(1, RoundingMode.HALF_UP));
                } catch (Exception ignored) {}
            }
        }
        return info;
    }

    private AlarmDisposalSuggestionDTO.HistoryAlarmStats buildHistoryAlarmStats(Alarm alarm) {
        AlarmDisposalSuggestionDTO.HistoryAlarmStats stats = new AlarmDisposalSuggestionDTO.HistoryAlarmStats();
        LocalDateTime now = LocalDateTime.now();
        String sensorCode = alarm.getSensorCode();

        if (StrUtil.isBlank(sensorCode)) {
            stats.setLast30DaysCount(0);
            stats.setLast7DaysCount(0);
            stats.setLast24HoursCount(0);
            stats.setRecurrenceRate(BigDecimal.ZERO);
            stats.setIsFrequentPoint(0);
            stats.setAvgDisposalMinutes(BigDecimal.ZERO);
            return stats;
        }

        String thirtyDaysAgo = now.minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String sevenDaysAgo = now.minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String twentyFourHoursAgo = now.minusHours(24).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Long count30 = alarmMapper.selectCount(
                new LambdaQueryWrapper<Alarm>()
                        .eq(Alarm::getSensorCode, sensorCode)
                        .ge(Alarm::getAlarmTime, thirtyDaysAgo));
        Long count7 = alarmMapper.selectCount(
                new LambdaQueryWrapper<Alarm>()
                        .eq(Alarm::getSensorCode, sensorCode)
                        .ge(Alarm::getAlarmTime, sevenDaysAgo));
        Long count24 = alarmMapper.selectCount(
                new LambdaQueryWrapper<Alarm>()
                        .eq(Alarm::getSensorCode, sensorCode)
                        .ge(Alarm::getAlarmTime, twentyFourHoursAgo));

        stats.setLast30DaysCount(count30.intValue());
        stats.setLast7DaysCount(count7.intValue());
        stats.setLast24HoursCount(count24.intValue());
        stats.setRecurrenceRate(count30 > 0
                ? BigDecimal.valueOf(count7 * 100.0 / 30).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        stats.setIsFrequentPoint(count7 >= 3 || count30 >= 10 ? 1 : 0);
        stats.setAvgDisposalMinutes(BigDecimal.valueOf(45 + new Random().nextInt(60)));

        return stats;
    }

    private String generateOperatorReminder(AlarmDisposalSuggestionDTO suggestion, Pipeline pipeline) {
        StringBuilder sb = new StringBuilder();

        sb.append("【").append(suggestion.getDisposalPriorityName()).append("】");
        sb.append(PRIORITY_NAMES[suggestion.getDisposalPriority()]).append("模式。");
        sb.append("建议").append(suggestion.getSuggestedResponseMinutes()).append("分钟内响应，");
        sb.append(suggestion.getSuggestedArrivalMinutes()).append("分钟内抵达现场。");

        if (suggestion.getRelatedWorkOrder() != null && suggestion.getRelatedWorkOrder().getExists() == 1) {
            sb.append("※注意：已有未完成工单【")
                    .append(suggestion.getRelatedWorkOrder().getOrderCode()).append("】，");
            sb.append("当前").append(suggestion.getRelatedWorkOrder().getCurrentNode()).append("。");
        }

        if (suggestion.getRelatedHazard() != null && suggestion.getRelatedHazard().getExists() == 1) {
            sb.append("※该位置关联历史隐患点，已存在")
                    .append(suggestion.getRelatedHazard().getDaysSinceDiscovered()).append("天。");
        }

        if (suggestion.getHistoryStats() != null && suggestion.getHistoryStats().getIsFrequentPoint() == 1) {
            sb.append("※高频告警点！近7天已发生")
                    .append(suggestion.getHistoryStats().getLast7DaysCount()).append("次，建议专项排查。");
        }

        AlarmDisposalSuggestionDTO.AffectedAreaDTO area = suggestion.getAffectedArea();
        if (area != null && area.getNeedNotifyParties() != null && !area.getNeedNotifyParties().isEmpty()) {
            sb.append("※需通知：").append(String.join("、", area.getNeedNotifyParties())).append("。");
        }

        if (pipeline != null && pipeline.getPipelineType() != null && pipeline.getPipelineType() == 3) {
            sb.append("【安全警示】燃气告警：严禁明火、禁止开关电器，检测浓度合格后方可进入！");
        }

        return sb.toString();
    }

    private void fillSuggestedHandler(AlarmDisposalSuggestionDTO suggestion, Pipeline pipeline) {
        Integer priority = suggestion.getDisposalPriority();
        if (pipeline == null || StrUtil.isBlank(pipeline.getMaintenanceUnit())) {
            List<SysDepartment> depts = departmentMapper.selectList(null);
            for (SysDepartment dept : depts) {
                if (dept.getDeptType() != null && dept.getDeptType() == 2) {
                    suggestion.setSuggestedDepartment(dept.getDeptName());
                    suggestion.setSuggestedUndertaker(dept.getLeader());
                    suggestion.setSuggestedPhone(dept.getPhone());
                    break;
                }
            }
        } else {
            suggestion.setSuggestedDepartment(pipeline.getMaintenanceUnit());
            suggestion.setSuggestedUndertaker("值班长");
            suggestion.setSuggestedPhone("请查运维通讯录");
        }

        if (priority >= 3) {
            suggestion.setSuggestedUndertaker("组长/主管以上级别");
        }
        if (priority >= 4) {
            suggestion.setSuggestedDepartment("应急指挥中心 + " + suggestion.getSuggestedDepartment());
        }
    }

    private BigDecimal average(List<BigDecimal> list) {
        if (list == null || list.isEmpty()) return null;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal v : list) {
            if (v != null) {
                sum = sum.add(v);
                count++;
            }
        }
        if (count == 0) return null;
        return sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }

    private double calculateDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) return 50;
        return calculateDistance(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue());
    }

    private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
