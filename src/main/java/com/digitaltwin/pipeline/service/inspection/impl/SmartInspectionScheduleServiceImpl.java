package com.digitaltwin.pipeline.service.inspection.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.mapper.asset.ManholeMapper;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.service.inspection.SmartInspectionScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartInspectionScheduleServiceImpl implements SmartInspectionScheduleService {

    private final PipelineMapper pipelineMapper;
    private final ValveMapper valveMapper;
    private final ManholeMapper manholeMapper;
    private final HazardMapper hazardMapper;
    private final WorkOrderMapper workOrderMapper;

    private static final String[] ASSET_TYPE_NAMES = {"", "管线", "阀门", "井盖", "隐患点", "维修工单"};
    private static final String[] RISK_LEVEL_NAMES = {"", "低风险", "中风险", "高风险", "特高风险"};

    @Override
    public SmartInspectionScheduleDTO generateSchedule(SmartInspectionScheduleQueryDTO query) {
        if (query == null) query = new SmartInspectionScheduleQueryDTO();
        LocalDate scheduleDate = query.getScheduleDate() != null ? query.getScheduleDate() : LocalDate.now();
        String strategy = StrUtil.isNotBlank(query.getScheduleStrategy()) ? query.getScheduleStrategy() : "RISK_FIRST";
        int minRiskLevel = query.getMinRiskLevel() != null ? query.getMinRiskLevel() : 1;
        int minUrgency = query.getMinUrgency() != null ? query.getMinUrgency() : 1;
        double maxHoursPerTeam = query.getMaxHoursPerTeam() != null ? query.getMaxHoursPerTeam().doubleValue() : 8.0;
        double maxKmPerTeam = query.getMaxKmPerTeam() != null ? query.getMaxKmPerTeam().doubleValue() : 15.0;

        List<SmartInspectionScheduleDTO.ScheduledInspectionTask> allPoints = new ArrayList<>();

        if (query.getIncludePendingWorkOrders() == null || query.getIncludePendingWorkOrders()) {
            allPoints.addAll(collectWorkOrderPoints(query, minUrgency));
        }
        if (query.getIncludeHazards() == null || query.getIncludeHazards()) {
            allPoints.addAll(collectHazardPoints(query, minRiskLevel));
        }
        if (query.getIncludeRoutinePoints() == null || query.getIncludeRoutinePoints()) {
            allPoints.addAll(collectRoutineValvePoints(query, minRiskLevel));
            allPoints.addAll(collectRoutineManholePoints(query, minRiskLevel));
            allPoints.addAll(collectRoutinePipelinePoints(query, minRiskLevel));
        }

        allPoints.sort(Comparator
                .comparingInt((SmartInspectionScheduleDTO.ScheduledInspectionTask t) ->
                        t.getPriorityScore() == null ? 0 : t.getPriorityScore()).reversed());

        SmartInspectionScheduleDTO result = new SmartInspectionScheduleDTO();
        result.setScheduleDate(scheduleDate);
        result.setGenerateTime(LocalDateTime.now());
        result.setTotalPoints(allPoints.size());

        List<SmartInspectionScheduleDTO.TeamTaskGroup> teamGroups = buildTeamGroups(
                allPoints, strategy, maxHoursPerTeam, maxKmPerTeam, query.getTeamCount());

        result.setTeamTaskGroups(teamGroups);

        BigDecimal totalDist = BigDecimal.ZERO;
        BigDecimal totalHours = BigDecimal.ZERO;
        for (SmartInspectionScheduleDTO.TeamTaskGroup g : teamGroups) {
            if (g.getTotalDistanceKm() != null) totalDist = totalDist.add(g.getTotalDistanceKm());
            if (g.getEstimatedHours() != null) totalHours = totalHours.add(g.getEstimatedHours());
        }
        result.setTotalDistanceKm(totalDist.setScale(2, RoundingMode.HALF_UP));
        result.setEstimatedTotalHours(totalHours.setScale(1, RoundingMode.HALF_UP));
        result.setSuggestedTeamCount(teamGroups.size());

        result.setAreaStatistics(buildAreaStatistics(allPoints));
        result.setRiskDistribution(buildRiskDistribution(allPoints));

        result.setPostponedPoints(new ArrayList<>());

        return result;
    }

    private List<SmartInspectionScheduleDTO.ScheduledInspectionTask> collectWorkOrderPoints(
            SmartInspectionScheduleQueryDTO query, int minUrgency) {
        List<SmartInspectionScheduleDTO.ScheduledInspectionTask> list = new ArrayList<>();
        List<WorkOrder> workOrders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrder>()
                        .notIn(WorkOrder::getStatus, 5, 6)
                        .ge(WorkOrder::getUrgency, minUrgency)
                        .last("LIMIT 500"));
        for (WorkOrder wo : workOrders) {
            if (query.getPipelineTypes() != null && !query.getPipelineTypes().isEmpty()
                    && wo.getPipelineType() != null && !query.getPipelineTypes().contains(wo.getPipelineType())) {
                continue;
            }
            if (CollUtil.isNotEmpty(query.getAreaCodes()) && StrUtil.isNotBlank(wo.getAreaCode())
                    && !query.getAreaCodes().contains(wo.getAreaCode())) continue;

            SmartInspectionScheduleDTO.ScheduledInspectionTask task = new SmartInspectionScheduleDTO.ScheduledInspectionTask();
            task.setPointId(100000L + wo.getId());
            task.setAssetType(5);
            task.setAssetTypeName(ASSET_TYPE_NAMES[5]);
            task.setRelatedId(wo.getId());
            task.setRelatedCode(wo.getOrderCode());
            task.setLocationName(StrUtil.isNotBlank(wo.getLocation()) ? wo.getLocation() : "工单位置");
            task.setLng(wo.getLng());
            task.setLat(wo.getLat());
            task.setAreaCode(wo.getAreaCode());
            int urgency = wo.getUrgency() != null ? wo.getUrgency() : 1;
            task.setUrgency(urgency);
            int rl = Math.min(4, Math.max(1, urgency + (urgency >= 3 ? 1 : 0)));
            task.setRiskLevel(rl);
            task.setRiskLevelName(RISK_LEVEL_NAMES[rl]);
            int score = 40 + urgency * 15;
            if (urgency >= 3) score += 10;
            task.setPriorityScore(Math.min(100, score));
            task.setInspectionFocus(buildWorkOrderFocus(wo));
            list.add(task);
        }
        return list;
    }

    private List<SmartInspectionScheduleDTO.ScheduledInspectionTask> collectHazardPoints(
            SmartInspectionScheduleQueryDTO query, int minRiskLevel) {
        List<SmartInspectionScheduleDTO.ScheduledInspectionTask> list = new ArrayList<>();
        List<Hazard> hazards = hazardMapper.selectList(
                new LambdaQueryWrapper<Hazard>()
                        .ne(Hazard::getStatus, 4)
                        .ge(Hazard::getRiskLevel, minRiskLevel)
                        .last("LIMIT 500"));
        for (Hazard h : hazards) {
            if (query.getPipelineTypes() != null && !query.getPipelineTypes().isEmpty()
                    && h.getPipelineType() != null && !query.getPipelineTypes().contains(h.getPipelineType())) continue;
            if (CollUtil.isNotEmpty(query.getAreaCodes()) && StrUtil.isNotBlank(h.getAreaCode())
                    && !query.getAreaCodes().contains(h.getAreaCode())) continue;

            SmartInspectionScheduleDTO.ScheduledInspectionTask task = new SmartInspectionScheduleDTO.ScheduledInspectionTask();
            task.setPointId(200000L + h.getId());
            task.setAssetType(4);
            task.setAssetTypeName(ASSET_TYPE_NAMES[4]);
            task.setRelatedId(h.getId());
            task.setRelatedCode(h.getHazardCode());
            task.setLocationName(StrUtil.isNotBlank(h.getLocation()) ? h.getLocation() : "隐患点位置");
            task.setLng(h.getLng());
            task.setLat(h.getLat());
            task.setAreaCode(h.getAreaCode());
            int rl = h.getRiskLevel() != null ? h.getRiskLevel() : 2;
            task.setRiskLevel(rl);
            task.setRiskLevelName(RISK_LEVEL_NAMES[rl]);
            task.setUrgency(Math.max(1, rl));
            int score = 30 + rl * 15 + (h.getRiskScore() != null ? h.getRiskScore() / 10 : 0);
            task.setPriorityScore(Math.min(100, score));
            task.setInspectionFocus("检查隐患:" + (StrUtil.isNotBlank(h.getHazardTypeDesc()) ? h.getHazardTypeDesc() : h.getDescription())
                    + "，核实整改情况，拍照留证");
            list.add(task);
        }
        return list;
    }

    private List<SmartInspectionScheduleDTO.ScheduledInspectionTask> collectRoutineValvePoints(
            SmartInspectionScheduleQueryDTO query, int minRiskLevel) {
        List<SmartInspectionScheduleDTO.ScheduledInspectionTask> list = new ArrayList<>();
        List<Valve> valves = valveMapper.selectList(new LambdaQueryWrapper<Valve>().last("LIMIT 200"));
        int count = 0;
        for (Valve v : valves) {
            if (count++ >= 30) break;
            if (query.getPipelineTypes() != null && !query.getPipelineTypes().isEmpty()
                    && v.getPipelineType() != null && !query.getPipelineTypes().contains(v.getPipelineType())) continue;
            if (CollUtil.isNotEmpty(query.getAreaCodes()) && StrUtil.isNotBlank(v.getAreaCode())
                    && !query.getAreaCodes().contains(v.getAreaCode())) continue;
            int rl = (v.getStatus() != null && v.getStatus() == 2) ? 3 : 1;
            if (rl < minRiskLevel) continue;

            SmartInspectionScheduleDTO.ScheduledInspectionTask task = new SmartInspectionScheduleDTO.ScheduledInspectionTask();
            task.setPointId(300000L + v.getId());
            task.setAssetType(2);
            task.setAssetTypeName(ASSET_TYPE_NAMES[2]);
            task.setRelatedId(v.getId());
            task.setRelatedCode(v.getValveCode());
            task.setLocationName(StrUtil.isNotBlank(v.getValveName()) ? v.getValveName() : v.getLocation());
            task.setLng(v.getLng());
            task.setLat(v.getLat());
            task.setAreaCode(v.getAreaCode());
            task.setRiskLevel(rl);
            task.setRiskLevelName(RISK_LEVEL_NAMES[rl]);
            task.setUrgency(1);
            task.setPriorityScore(10 + rl * 8);
            task.setInspectionFocus("检查阀门有无泄漏、锈蚀，开关是否灵活，螺栓紧固情况");
            list.add(task);
        }
        return list;
    }

    private List<SmartInspectionScheduleDTO.ScheduledInspectionTask> collectRoutineManholePoints(
            SmartInspectionScheduleQueryDTO query, int minRiskLevel) {
        List<SmartInspectionScheduleDTO.ScheduledInspectionTask> list = new ArrayList<>();
        List<Manhole> manholes = manholeMapper.selectList(new LambdaQueryWrapper<Manhole>().last("LIMIT 200"));
        int count = 0;
        for (Manhole m : manholes) {
            if (count++ >= 20) break;
            if (query.getPipelineTypes() != null && !query.getPipelineTypes().isEmpty()
                    && m.getPipelineType() != null && !query.getPipelineTypes().contains(m.getPipelineType())) continue;
            if (CollUtil.isNotEmpty(query.getAreaCodes()) && StrUtil.isNotBlank(m.getAreaCode())
                    && !query.getAreaCodes().contains(m.getAreaCode())) continue;
            int rl = (m.getCondition() != null && m.getCondition() >= 3) ? 2 : 1;
            if (rl < minRiskLevel) continue;

            SmartInspectionScheduleDTO.ScheduledInspectionTask task = new SmartInspectionScheduleDTO.ScheduledInspectionTask();
            task.setPointId(400000L + m.getId());
            task.setAssetType(3);
            task.setAssetTypeName(ASSET_TYPE_NAMES[3]);
            task.setRelatedId(m.getId());
            task.setRelatedCode(m.getManholeCode());
            task.setLocationName(m.getLocation());
            task.setLng(m.getLng());
            task.setLat(m.getLat());
            task.setAreaCode(m.getAreaCode());
            task.setRiskLevel(rl);
            task.setRiskLevelName(RISK_LEVEL_NAMES[rl]);
            task.setUrgency(1);
            task.setPriorityScore(5 + rl * 5);
            task.setInspectionFocus("检查井盖完整性、有无沉降、井室有无积水杂物");
            list.add(task);
        }
        return list;
    }

    private List<SmartInspectionScheduleDTO.ScheduledInspectionTask> collectRoutinePipelinePoints(
            SmartInspectionScheduleQueryDTO query, int minRiskLevel) {
        List<SmartInspectionScheduleDTO.ScheduledInspectionTask> list = new ArrayList<>();
        List<Pipeline> pipelines = pipelineMapper.selectList(new LambdaQueryWrapper<Pipeline>().last("LIMIT 100"));
        int count = 0;
        for (Pipeline p : pipelines) {
            if (count++ >= 10) break;
            if (query.getPipelineTypes() != null && !query.getPipelineTypes().isEmpty()
                    && p.getPipelineType() != null && !query.getPipelineTypes().contains(p.getPipelineType())) continue;
            if (CollUtil.isNotEmpty(query.getAreaCodes()) && StrUtil.isNotBlank(p.getAreaCode())
                    && !query.getAreaCodes().contains(p.getAreaCode())) continue;
            int rl = (p.getPressureLevel() != null && p.getPressureLevel() >= 3) ? 2 : 1;
            if (rl < minRiskLevel) continue;

            SmartInspectionScheduleDTO.ScheduledInspectionTask task = new SmartInspectionScheduleDTO.ScheduledInspectionTask();
            task.setPointId(500000L + p.getId());
            task.setAssetType(1);
            task.setAssetTypeName(ASSET_TYPE_NAMES[1]);
            task.setRelatedId(p.getId());
            task.setRelatedCode(p.getPipelineCode());
            task.setLocationName(p.getRoadName() + " " + p.getStartPoint() + "-" + p.getEndPoint());
            task.setLng(p.getStartLng());
            task.setLat(p.getStartLat());
            task.setAreaCode(p.getAreaCode());
            task.setRiskLevel(rl);
            task.setRiskLevelName(RISK_LEVEL_NAMES[rl]);
            task.setUrgency(1);
            task.setPriorityScore(5 + rl * 5 + (p.getAge() != null ? Math.min(10, p.getAge() / 2) : 0));
            task.setInspectionFocus("沿管线巡视：有无开挖、塌陷、占压、地表异常、标志桩完好情况");
            list.add(task);
        }
        return list;
    }

    private List<SmartInspectionScheduleDTO.TeamTaskGroup> buildTeamGroups(
            List<SmartInspectionScheduleDTO.ScheduledInspectionTask> allPoints, String strategy,
            double maxHoursPerTeam, double maxKmPerTeam, Integer requestedTeamCount) {
        if (CollUtil.isEmpty(allPoints)) return new ArrayList<>();

        Map<String, List<SmartInspectionScheduleDTO.ScheduledInspectionTask>> areaMap = allPoints.stream()
                .collect(Collectors.groupingBy(t -> StrUtil.isNotBlank(t.getAreaCode()) ? t.getAreaCode() : "UNKNOWN"));

        List<List<SmartInspectionScheduleDTO.ScheduledInspectionTask>> areaClusters = new ArrayList<>(areaMap.values());
        int suggestedTeams = Math.max(1, Math.min(6, (int) Math.ceil(allPoints.size() / 15.0)));
        if (requestedTeamCount != null && requestedTeamCount > 0) {
            suggestedTeams = Math.min(suggestedTeams, requestedTeamCount);
        }

        List<List<SmartInspectionScheduleDTO.ScheduledInspectionTask>> assigned = new ArrayList<>();
        for (int i = 0; i < suggestedTeams; i++) assigned.add(new ArrayList<>());

        int teamCursor = 0;
        Comparator<SmartInspectionScheduleDTO.ScheduledInspectionTask> comparator;
        if ("AREA_FIRST".equals(strategy)) {
            comparator = Comparator.comparing(
                    (SmartInspectionScheduleDTO.ScheduledInspectionTask t) -> StrUtil.nullToDefault(t.getAreaCode(), ""))
                    .thenComparingInt(t -> t.getPriorityScore() == null ? 0 : t.getPriorityScore()).reversed();
        } else if ("ROUTE_OPTIMAL".equals(strategy)) {
            comparator = Comparator.comparingInt(
                    (SmartInspectionScheduleDTO.ScheduledInspectionTask t) -> t.getPriorityScore() == null ? 0 : t.getPriorityScore()).reversed();
        } else {
            comparator = Comparator.comparingInt(
                    (SmartInspectionScheduleDTO.ScheduledInspectionTask t) -> t.getPriorityScore() == null ? 0 : t.getPriorityScore()).reversed();
        }

        allPoints.sort(comparator);
        for (SmartInspectionScheduleDTO.ScheduledInspectionTask t : allPoints) {
            assigned.get(teamCursor % suggestedTeams).add(t);
            teamCursor++;
        }

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        List<SmartInspectionScheduleDTO.TeamTaskGroup> result = new ArrayList<>();
        int teamNo = 1;
        for (List<SmartInspectionScheduleDTO.ScheduledInspectionTask> group : assigned) {
            if (CollUtil.isEmpty(group)) { teamNo++; continue; }

            sortByRoute(group);
            fillRouteMeta(group);

            SmartInspectionScheduleDTO.TeamTaskGroup tg = new SmartInspectionScheduleDTO.TeamTaskGroup();
            tg.setTeamNo(teamNo++);
            Set<String> areas = group.stream().map(SmartInspectionScheduleDTO.ScheduledInspectionTask::getAreaCode)
                    .filter(StrUtil::isNotBlank).collect(Collectors.toSet());
            tg.setAssignedArea(String.join("/", areas));
            tg.setTaskCount(group.size());

            BigDecimal totalDist = BigDecimal.ZERO;
            for (SmartInspectionScheduleDTO.ScheduledInspectionTask t : group) {
                if (t.getDistanceFromPrev() != null) {
                    totalDist = totalDist.add(t.getDistanceFromPrev());
                }
            }
            BigDecimal distKm = totalDist.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            tg.setTotalDistanceKm(distKm);
            BigDecimal hours = distKm.add(BigDecimal.valueOf(group.size() * 0.25)).setScale(1, RoundingMode.HALF_UP);
            tg.setEstimatedHours(hours);

            LocalTime start = LocalTime.of(8, 30);
            int addMinutes = (teamNo - 1) * 5;
            start = start.plusMinutes(addMinutes);
            tg.setSuggestedStartTime(start.format(tf));
            int minutes = hours.multiply(BigDecimal.valueOf(60)).intValue();
            tg.setSuggestedEndTime(start.plusMinutes(minutes).format(tf));

            LocalTime cursor = start;
            for (SmartInspectionScheduleDTO.ScheduledInspectionTask t : group) {
                if (t.getDistanceFromPrev() != null) {
                    double walkMin = t.getDistanceFromPrev().doubleValue() / 80.0;
                    cursor = cursor.plusMinutes((long) walkMin);
                }
                t.setEstimatedArrival(cursor.format(tf));
                cursor = cursor.plusMinutes(15);
            }

            tg.setOrderedTasks(group);
            int maxRisk = group.stream()
                    .mapToInt(t -> t.getRiskLevel() != null ? t.getRiskLevel() : 0)
                    .max().orElse(1);
            tg.setMaxRisk(maxRisk);

            result.add(tg);
        }
        return result;
    }

    private void sortByRoute(List<SmartInspectionScheduleDTO.ScheduledInspectionTask> list) {
        if (CollUtil.isEmpty(list) || list.size() < 3) return;

        for (int i = 0; i < list.size() - 1; i++) {
            SmartInspectionScheduleDTO.ScheduledInspectionTask curr = list.get(i);
            int bestIdx = i + 1;
            double bestDist = Double.MAX_VALUE;
            for (int j = i + 1; j < list.size(); j++) {
                SmartInspectionScheduleDTO.ScheduledInspectionTask next = list.get(j);
                double d = calculateDistance(curr.getLng(), curr.getLat(), next.getLng(), next.getLat());
                if (d < bestDist) { bestDist = d; bestIdx = j; }
            }
            if (bestIdx != i + 1) {
                SmartInspectionScheduleDTO.ScheduledInspectionTask tmp = list.get(i + 1);
                list.set(i + 1, list.get(bestIdx));
                list.set(bestIdx, tmp);
            }
        }
    }

    private void fillRouteMeta(List<SmartInspectionScheduleDTO.ScheduledInspectionTask> list) {
        if (CollUtil.isEmpty(list)) return;
        for (int i = 0; i < list.size(); i++) {
            SmartInspectionScheduleDTO.ScheduledInspectionTask t = list.get(i);
            t.setOrderNo(i + 1);
            if (i == 0) {
                t.setDistanceFromPrev(BigDecimal.ZERO);
            } else {
                SmartInspectionScheduleDTO.ScheduledInspectionTask prev = list.get(i - 1);
                double d = calculateDistance(prev.getLng(), prev.getLat(), t.getLng(), t.getLat());
                t.setDistanceFromPrev(BigDecimal.valueOf(d).setScale(1, RoundingMode.HALF_UP));
            }
        }
    }

    private List<SmartInspectionScheduleDTO.AreaStatistics> buildAreaStatistics(
            List<SmartInspectionScheduleDTO.ScheduledInspectionTask> all) {
        Map<String, List<SmartInspectionScheduleDTO.ScheduledInspectionTask>> map = all.stream()
                .collect(Collectors.groupingBy(t -> StrUtil.isNotBlank(t.getAreaCode()) ? t.getAreaCode() : "未知"));
        List<SmartInspectionScheduleDTO.AreaStatistics> result = new ArrayList<>();
        for (Map.Entry<String, List<SmartInspectionScheduleDTO.ScheduledInspectionTask>> e : map.entrySet()) {
            SmartInspectionScheduleDTO.AreaStatistics s = new SmartInspectionScheduleDTO.AreaStatistics();
            s.setAreaCode(e.getKey());
            s.setAreaName(e.getKey());
            s.setPointCount(e.getValue().size());
            double dist = 0;
            for (int i = 1; i < e.getValue().size(); i++) {
                SmartInspectionScheduleDTO.ScheduledInspectionTask p0 = e.getValue().get(i - 1);
                SmartInspectionScheduleDTO.ScheduledInspectionTask p1 = e.getValue().get(i);
                dist += calculateDistance(p0.getLng(), p0.getLat(), p1.getLng(), p1.getLat());
            }
            s.setDistanceKm(BigDecimal.valueOf(dist / 1000).setScale(2, RoundingMode.HALF_UP));
            result.add(s);
        }
        result.sort(Comparator.comparingInt(SmartInspectionScheduleDTO.AreaStatistics::getPointCount).reversed());
        return result;
    }

    private SmartInspectionScheduleDTO.RiskDistribution buildRiskDistribution(
            List<SmartInspectionScheduleDTO.ScheduledInspectionTask> all) {
        SmartInspectionScheduleDTO.RiskDistribution r = new SmartInspectionScheduleDTO.RiskDistribution();
        int c1 = 0, c2 = 0, c3 = 0, c4 = 0, cu = 0;
        for (SmartInspectionScheduleDTO.ScheduledInspectionTask t : all) {
            int rl = t.getRiskLevel() != null ? t.getRiskLevel() : 1;
            if (rl == 4) c4++;
            else if (rl == 3) c3++;
            else if (rl == 2) c2++;
            else c1++;
            if (t.getAssetType() != null && t.getAssetType() == 5
                    && t.getUrgency() != null && t.getUrgency() >= 3) cu++;
        }
        r.setLowCount(c1);
        r.setMediumCount(c2);
        r.setHighCount(c3);
        r.setCriticalCount(c4);
        r.setUrgentWorkOrders(cu);
        return r;
    }

    private String buildWorkOrderFocus(WorkOrder wo) {
        StringBuilder sb = new StringBuilder();
        sb.append("工单处置:").append(wo.getOrderCode()).append(" - ");
        if (wo.getTitle() != null) sb.append(wo.getTitle());
        sb.append("。现场核实情况，拍照上传，处置完成后提交验收");
        return sb.toString();
    }

    private double calculateDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) return 100;
        return calculateDistance(lng1.doubleValue(), lat1.doubleValue(), lng2.doubleValue(), lat2.doubleValue());
    }

    private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double r = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
}
