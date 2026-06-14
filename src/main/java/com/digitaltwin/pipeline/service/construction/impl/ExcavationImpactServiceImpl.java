package com.digitaltwin.pipeline.service.construction.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.construction.ExcavationImpactAssessmentDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.entity.auth.SysDepartment;
import com.digitaltwin.pipeline.entity.construction.EmergencyValveSuggestion;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.construction.PipelineConflict;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import com.digitaltwin.pipeline.mapper.asset.ManholeMapper;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.mapper.auth.SysDepartmentMapper;
import com.digitaltwin.pipeline.mapper.construction.EmergencyValveSuggestionMapper;
import com.digitaltwin.pipeline.mapper.construction.ExcavationApplicationMapper;
import com.digitaltwin.pipeline.mapper.construction.PipelineConflictMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.mapper.sensor.SensorMapper;
import com.digitaltwin.pipeline.service.construction.ExcavationImpactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcavationImpactServiceImpl implements ExcavationImpactService {

    private final ExcavationApplicationMapper applicationMapper;
    private final PipelineMapper pipelineMapper;
    private final ValveMapper valveMapper;
    private final ManholeMapper manholeMapper;
    private final PipelineConflictMapper conflictMapper;
    private final EmergencyValveSuggestionMapper valveSuggestionMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SensorMapper sensorMapper;
    private final AlarmMapper alarmMapper;
    private final HazardMapper hazardMapper;

    private static final String[] PIPELINE_TYPE_NAMES = {"", "给水", "排水", "燃气", "电力", "通信", "热力", "工业"};
    private static final String[] MATERIAL_NAMES = {"", "铸铁", "钢管", "PE", "PVC", "混凝土", "其他"};
    private static final String[] VALVE_TYPE_NAMES = {"", "闸阀", "蝶阀", "球阀", "截止阀", "止回阀"};
    private static final String[] MANHOLE_TYPE_NAMES = {"", "雨水", "污水", "给水", "电力", "通信", "燃气", "热力", "综合"};
    private static final String[] IMPACT_LEVEL_NAMES = {"", "轻微影响", "一般影响", "较大影响", "重大影响"};

    @Override
    public ExcavationImpactAssessmentDTO comprehensiveAssessment(Long applicationId) {
        ExcavationApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException(ResultCode.EXCAVATION_NOT_FOUND);
        }

        ExcavationImpactAssessmentDTO result = new ExcavationImpactAssessmentDTO();

        List<Pipeline> areaPipelines = pipelineMapper.selectByAreaAndType(app.getAreaCode(), null);
        List<Valve> allValves = valveMapper.selectList(null);
        List<Manhole> allManholes = manholeMapper.selectList(null);

        List<Pipeline> involvedPipelines = areaPipelines.stream()
                .filter(p -> isNearExcavationArea(p, app, 100))
                .collect(Collectors.toList());

        Set<Long> involvedPipelineIds = involvedPipelines.stream()
                .map(Pipeline::getId).collect(Collectors.toSet());

        List<Valve> involvedValves = allValves.stream()
                .filter(v -> v.getPipelineId() != null && involvedPipelineIds.contains(v.getPipelineId()))
                .collect(Collectors.toList());

        List<Manhole> involvedManholes = allManholes.stream()
                .filter(m -> m.getPipelineId() != null && involvedPipelineIds.contains(m.getPipelineId()))
                .collect(Collectors.toList());

        List<ExcavationImpactAssessmentDTO.AffectedPipelineInfo> pipelineInfos =
                buildAffectedPipelineInfos(involvedPipelines, app);

        List<ExcavationImpactAssessmentDTO.AffectedValveInfo> valveInfos =
                buildAffectedValveInfos(involvedValves, app);

        List<ExcavationImpactAssessmentDTO.AffectedManholeInfo> manholeInfos =
                buildAffectedManholeInfos(involvedManholes, app);

        Map<String, Set<Integer>> roadPipelineMap = new LinkedHashMap<>();
        for (Pipeline p : involvedPipelines) {
            if (StrUtil.isNotBlank(p.getRoadName())) {
                roadPipelineMap.computeIfAbsent(p.getRoadName(), k -> new HashSet<>()).add(p.getPipelineType());
            }
        }
        List<ExcavationImpactAssessmentDTO.AffectedRoadInfo> roadInfos =
                buildAffectedRoadInfos(roadPipelineMap, app);

        List<ExcavationImpactAssessmentDTO.NotificationDept> notifications =
                buildNotificationDepts(involvedPipelines, involvedValves, involvedManholes);

        List<PipelineConflict> conflicts = conflictMapper.selectByApplicationId(app.getId());
        List<EmergencyValveSuggestion> valveSuggestions = valveSuggestionMapper.selectByApplicationId(app.getId());

        ExcavationImpactAssessmentDTO.ImpactSummary summary =
                buildSummary(involvedPipelines, involvedValves, involvedManholes, roadInfos, notifications, conflicts);

        int impactScore = calculateImpactScore(summary, conflicts);
        int impactLevel = mapImpactLevel(impactScore);

        result.setImpactScore(impactScore);
        result.setImpactLevel(impactLevel);
        result.setImpactLevelName(IMPACT_LEVEL_NAMES[impactLevel]);
        result.setPassed(impactLevel <= 2 && summary.getUrgentDepartmentCount() == 0);
        result.setImpactBoundary(app.getGeometry());
        result.setEstimatedImpactArea(app.getArea());
        result.setAffectedPipelines(pipelineInfos);
        result.setAffectedValves(valveInfos);
        result.setAffectedManholes(manholeInfos);
        result.setAffectedRoads(roadInfos);
        result.setSuggestedNotifications(notifications);
        result.setConflicts(conflicts);
        result.setValveSuggestions(valveSuggestions);
        result.setSummary(summary);
        result.setReviewOpinion(generateComprehensiveOpinion(impactLevel, summary, conflicts, notifications));

        return result;
    }

    @Override
    public String generateImpactReport(Long applicationId) {
        ExcavationImpactAssessmentDTO assessment = comprehensiveAssessment(applicationId);
        ExcavationApplication app = applicationMapper.selectById(applicationId);

        StringBuilder sb = new StringBuilder();
        sb.append("===== 开挖施工影响评估报告 =====\n");
        sb.append("项目名称：").append(app.getProjectName()).append("\n");
        sb.append("申请编号：").append(app.getApplicationCode()).append("\n");
        sb.append("申请单位：").append(app.getApplicantUnit()).append("\n");
        sb.append("开挖区域：").append(app.getAreaDescription()).append("\n");
        sb.append("评估时间：").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        sb.append("【综合评估】\n");
        sb.append("影响等级：").append(assessment.getImpactLevelName())
                .append(" (评分").append(assessment.getImpactScore()).append("分)\n");
        sb.append("评估结论：").append(assessment.getPassed() ? "建议通过" : "需修改方案后重新提交").append("\n\n");
        sb.append("【影响统计】\n");
        ExcavationImpactAssessmentDTO.ImpactSummary s = assessment.getSummary();
        sb.append("涉及管线：").append(s.getTotalPipelines()).append("条（")
                .append(s.getPipelineTypeCount()).append("种类型）\n");
        sb.append("涉及阀门：").append(s.getTotalValves()).append("个（需关阀")
                .append(s.getNeedClosureValveCount()).append("个）\n");
        sb.append("涉及井盖：").append(s.getTotalManholes()).append("个（需防护")
                .append(s.getNeedProtectionManholeCount()).append("个）\n");
        sb.append("影响道路：").append(s.getTotalRoads()).append("条\n");
        sb.append("需通知部门：").append(s.getTotalDepartments()).append("个（紧急")
                .append(s.getUrgentDepartmentCount()).append("个）\n");
        sb.append("预计影响用户：").append(s.getEstimatedAffectedUsers()).append("户\n\n");
        sb.append("【评估意见】\n").append(assessment.getReviewOpinion());
        return sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(Long applicationId) {
        ExcavationApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException(ResultCode.EXCAVATION_NOT_FOUND);
        }
        if (app.getStatus() != 1) {
            throw new BusinessException(ResultCode.STATUS_ERROR);
        }
        app.setStatus(2);
        applicationMapper.updateById(app);
    }

    private List<ExcavationImpactAssessmentDTO.AffectedPipelineInfo> buildAffectedPipelineInfos(
            List<Pipeline> pipelines, ExcavationApplication app) {
        List<ExcavationImpactAssessmentDTO.AffectedPipelineInfo> result = new ArrayList<>();
        for (Pipeline p : pipelines) {
            ExcavationImpactAssessmentDTO.AffectedPipelineInfo info =
                    new ExcavationImpactAssessmentDTO.AffectedPipelineInfo();
            info.setPipelineId(p.getId());
            info.setPipelineCode(p.getPipelineCode());
            info.setPipelineName(p.getPipelineName());
            info.setPipelineType(p.getPipelineType());
            info.setPipelineTypeName(p.getPipelineType() != null && p.getPipelineType() < PIPELINE_TYPE_NAMES.length
                    ? PIPELINE_TYPE_NAMES[p.getPipelineType()] : "未知");
            info.setDiameter(p.getDiameter());
            info.setLength(p.getLength());
            info.setBuriedDepth(p.getBuriedDepth());
            info.setMaterial(p.getMaterial());
            info.setMaterialName(p.getMaterial() != null && p.getMaterial() < MATERIAL_NAMES.length
                    ? MATERIAL_NAMES[p.getMaterial()] : "未知");
            info.setOwnerUnit(p.getOwnerUnit());
            info.setMaintenanceUnit(p.getMaintenanceUnit());

            double dist = calculateDistanceToCenter(p, app);
            info.setMinDistance(BigDecimal.valueOf(dist).setScale(2, RoundingMode.HALF_UP));

            int influenceLevel;
            if (dist < 2) influenceLevel = 4;
            else if (dist < 5) influenceLevel = 3;
            else if (dist < 10) influenceLevel = 2;
            else influenceLevel = 1;
            info.setInfluenceLevel(influenceLevel);

            info.setRiskDescription(generatePipelineRiskDesc(p, dist));
            info.setProtectionSuggestion(generatePipelineProtection(p, dist));

            result.add(info);
        }
        result.sort(Comparator.comparingInt(ExcavationImpactAssessmentDTO.AffectedPipelineInfo::getInfluenceLevel).reversed()
                .thenComparing(a -> a.getMinDistance() != null ? a.getMinDistance().doubleValue() : 999));
        return result;
    }

    private List<ExcavationImpactAssessmentDTO.AffectedValveInfo> buildAffectedValveInfos(
            List<Valve> valves, ExcavationApplication app) {
        List<ExcavationImpactAssessmentDTO.AffectedValveInfo> result = new ArrayList<>();
        for (Valve v : valves) {
            ExcavationImpactAssessmentDTO.AffectedValveInfo info =
                    new ExcavationImpactAssessmentDTO.AffectedValveInfo();
            info.setValveId(v.getId());
            info.setValveCode(v.getValveCode());
            info.setValveName(v.getValveName());
            info.setValveType(v.getValveType());
            info.setValveTypeName(v.getValveType() != null && v.getValveType() < VALVE_TYPE_NAMES.length
                    ? VALVE_TYPE_NAMES[v.getValveType()] : "未知");
            info.setDiameter(v.getDiameter());
            info.setPipelineId(v.getPipelineId());
            info.setPipelineCode(v.getPipelineCode());
            info.setLocation(v.getLocation());
            info.setOwnerUnit(v.getOwnerUnit());

            double dist = calculateDistance(v.getLng(), v.getLat(), app.getCenterLng(), app.getCenterLat());
            info.setDistance(BigDecimal.valueOf(dist).setScale(2, RoundingMode.HALF_UP));
            info.setNeedClosure(dist < 10 && (v.getStatus() != null && v.getStatus() == 1) ? 1 : 0);

            result.add(info);
        }
        result.sort(Comparator.comparing(a -> a.getDistance() != null ? a.getDistance().doubleValue() : 999));
        return result;
    }

    private List<ExcavationImpactAssessmentDTO.AffectedManholeInfo> buildAffectedManholeInfos(
            List<Manhole> manholes, ExcavationApplication app) {
        List<ExcavationImpactAssessmentDTO.AffectedManholeInfo> result = new ArrayList<>();
        for (Manhole m : manholes) {
            ExcavationImpactAssessmentDTO.AffectedManholeInfo info =
                    new ExcavationImpactAssessmentDTO.AffectedManholeInfo();
            info.setManholeId(m.getId());
            info.setManholeCode(m.getManholeCode());
            info.setManholeName(m.getManholeName());
            info.setManholeType(m.getManholeType());
            info.setManholeTypeName(m.getManholeType() != null && m.getManholeType() < MANHOLE_TYPE_NAMES.length
                    ? MANHOLE_TYPE_NAMES[m.getManholeType()] : "未知");
            info.setPipelineId(m.getPipelineId());
            info.setRoadName(m.getRoadName());
            info.setOwnerUnit(m.getOwnerUnit());

            double dist = calculateDistance(m.getLng(), m.getLat(), app.getCenterLng(), app.getCenterLat());
            info.setDistance(BigDecimal.valueOf(dist).setScale(2, RoundingMode.HALF_UP));
            info.setNeedProtection(dist < 5 && m.getStatus() != null && m.getStatus() == 1 ? 1 : 0);

            result.add(info);
        }
        result.sort(Comparator.comparing(a -> a.getDistance() != null ? a.getDistance().doubleValue() : 999));
        return result;
    }

    private List<ExcavationImpactAssessmentDTO.AffectedRoadInfo> buildAffectedRoadInfos(
            Map<String, Set<Integer>> roadPipelineMap, ExcavationApplication app) {
        List<ExcavationImpactAssessmentDTO.AffectedRoadInfo> result = new ArrayList<>();
        for (Map.Entry<String, Set<Integer>> entry : roadPipelineMap.entrySet()) {
            ExcavationImpactAssessmentDTO.AffectedRoadInfo info =
                    new ExcavationImpactAssessmentDTO.AffectedRoadInfo();
            info.setRoadName(entry.getKey());

            Set<Integer> types = entry.getValue();
            int typeCount = types.size();
            info.setRoadLevel(estimateRoadLevel(app.getAreaName(), entry.getKey()));
            info.setAffectedLength(BigDecimal.valueOf(50 + new Random().nextInt(200)));
            info.setOccupiedLanes(estimateLanes(app.getDepth()));
            info.setPipelineTypeCount(typeCount);
            info.setPipelineTypeNames(types.stream()
                    .filter(t -> t != null && t < PIPELINE_TYPE_NAMES.length)
                    .map(t -> PIPELINE_TYPE_NAMES[t])
                    .collect(Collectors.toList()));

            int trafficImpact = 1;
            if (info.getRoadLevel() == 1 && info.getOccupiedLanes() >= 2) trafficImpact = 4;
            else if (info.getRoadLevel() == 1 && info.getOccupiedLanes() == 1) trafficImpact = 3;
            else if (info.getRoadLevel() == 2) trafficImpact = 2;
            else if (typeCount >= 4) trafficImpact = 3;
            info.setTrafficImpact(trafficImpact);
            info.setTrafficSuggestion(generateTrafficSuggestion(info));

            result.add(info);
        }
        result.sort(Comparator.comparingInt(ExcavationImpactAssessmentDTO.AffectedRoadInfo::getTrafficImpact).reversed());
        return result;
    }

    private List<ExcavationImpactAssessmentDTO.NotificationDept> buildNotificationDepts(
            List<Pipeline> pipelines, List<Valve> valves, List<Manhole> manholes) {
        Map<Long, ExcavationImpactAssessmentDTO.NotificationDept> deptMap = new LinkedHashMap<>();

        List<SysDepartment> allDepts = departmentMapper.selectList(null);

        Set<Integer> pipelineTypes = pipelines.stream()
                .map(Pipeline::getPipelineType).filter(Objects::nonNull).collect(Collectors.toSet());

        Set<String> ownerUnits = new HashSet<>();
        pipelines.forEach(p -> { if (StrUtil.isNotBlank(p.getOwnerUnit())) ownerUnits.add(p.getOwnerUnit()); });
        valves.forEach(v -> { if (StrUtil.isNotBlank(v.getOwnerUnit())) ownerUnits.add(v.getOwnerUnit()); });
        manholes.forEach(m -> { if (StrUtil.isNotBlank(m.getOwnerUnit())) ownerUnits.add(m.getOwnerUnit()); });

        boolean hasGas = pipelineTypes.contains(3);
        boolean hasPower = pipelineTypes.contains(4);
        boolean hasWater = pipelineTypes.contains(1) || pipelineTypes.contains(2);
        boolean hasMultiple = pipelineTypes.size() >= 3;

        for (SysDepartment dept : allDepts) {
            ExcavationImpactAssessmentDTO.NotificationDept nd =
                    new ExcavationImpactAssessmentDTO.NotificationDept();
            nd.setDeptId(dept.getId());
            nd.setDeptName(dept.getDeptName());
            nd.setLeader(dept.getLeader());
            nd.setPhone(dept.getPhone());
            nd.setPipelineTypes(dept.getPipelineTypes());
            nd.setUrgency(1);
            nd.setReason("");

            Integer deptType = dept.getDeptType();
            boolean match = false;

            if (deptType == 1) {
                match = true;
                nd.setReason("管理部门需知悉施工情况并协调各方");
                nd.setUrgency(2);
            } else if (deptType == 3) {
                match = true;
                nd.setReason("审批部门需进行施工许可审核");
                nd.setUrgency(2);
            } else if (deptType == 2 && StrUtil.isNotBlank(dept.getPipelineTypes())) {
                List<String> deptTypes = Arrays.asList(dept.getPipelineTypes().split(","));
                for (Integer pt : pipelineTypes) {
                    if (deptTypes.contains(String.valueOf(pt))) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    if ((hasGas && deptTypes.contains("3")) || (hasPower && deptTypes.contains("4"))) {
                        nd.setReason("所管辖的高风险管线位于施工影响范围，需现场交底");
                        nd.setUrgency(4);
                    } else if (hasWater && (deptTypes.contains("1") || deptTypes.contains("2"))) {
                        nd.setReason("所管辖的给排水管线位于施工范围，需协调保护方案");
                        nd.setUrgency(3);
                    } else {
                        nd.setReason("所管辖管线位于施工影响范围，需知悉并配合");
                        nd.setUrgency(2);
                    }
                }
            } else if (deptType == 4 || deptType == 5) {
                for (String unit : ownerUnits) {
                    if (StrUtil.isNotBlank(unit) && dept.getDeptName().contains(unit.substring(0, Math.min(2, unit.length())))) {
                        match = true;
                        nd.setReason("产权/施工单位需参与现场协调");
                        nd.setUrgency(3);
                        break;
                    }
                }
                if (hasMultiple) {
                    match = true;
                    if (!deptMap.containsKey(dept.getId())) {
                        nd.setReason("涉及多类管线交叉施工，需专业单位配合");
                        nd.setUrgency(Math.max(nd.getUrgency(), 3));
                    }
                }
            }

            if (match) {
                deptMap.merge(dept.getId(), nd, (existing, newOne) -> {
                    existing.setUrgency(Math.max(existing.getUrgency(), newOne.getUrgency()));
                    if (StrUtil.isBlank(existing.getReason())) {
                        existing.setReason(newOne.getReason());
                    } else if (!existing.getReason().equals(newOne.getReason())) {
                        existing.setReason(existing.getReason() + "；" + newOne.getReason());
                    }
                    return existing;
                });
            }
        }

        List<ExcavationImpactAssessmentDTO.NotificationDept> result = new ArrayList<>(deptMap.values());
        result.sort(Comparator.comparingInt(ExcavationImpactAssessmentDTO.NotificationDept::getUrgency).reversed());
        return result;
    }

    private ExcavationImpactAssessmentDTO.ImpactSummary buildSummary(
            List<Pipeline> pipelines, List<Valve> valves, List<Manhole> manholes,
            List<ExcavationImpactAssessmentDTO.AffectedRoadInfo> roads,
            List<ExcavationImpactAssessmentDTO.NotificationDept> depts,
            List<PipelineConflict> conflicts) {
        ExcavationImpactAssessmentDTO.ImpactSummary s = new ExcavationImpactAssessmentDTO.ImpactSummary();
        s.setTotalPipelines(pipelines.size());
        s.setPipelineTypeCount((int) pipelines.stream().map(Pipeline::getPipelineType)
                .filter(Objects::nonNull).distinct().count());
        s.setHighRiskPipelineCount((int) conflicts.stream().filter(c -> c.getConflictLevel() != null && c.getConflictLevel() >= 3).count());
        s.setGasPipelineCount((int) pipelines.stream().filter(p -> p.getPipelineType() != null && p.getPipelineType() == 3).count());
        s.setPowerPipelineCount((int) pipelines.stream().filter(p -> p.getPipelineType() != null && p.getPipelineType() == 4).count());
        s.setWaterPipelineCount((int) pipelines.stream().filter(p -> p.getPipelineType() != null
                && (p.getPipelineType() == 1 || p.getPipelineType() == 2)).count());
        s.setTotalValves(valves.size());
        s.setNeedClosureValveCount((int) valves.stream().filter(v -> {
            double dist = calculateDistance(v.getLng(), v.getLat(), null, null);
            return dist < 10;
        }).count());
        s.setTotalManholes(manholes.size());
        s.setNeedProtectionManholeCount((int) manholes.stream().filter(m -> {
            double dist = calculateDistance(m.getLng(), m.getLat(), null, null);
            return dist < 5;
        }).count());
        s.setTotalRoads(roads.size());
        s.setMainRoadCount((int) roads.stream().filter(r -> r.getRoadLevel() != null && r.getRoadLevel() == 1).count());
        s.setTotalDepartments(depts.size());
        s.setUrgentDepartmentCount((int) depts.stream().filter(d -> d.getUrgency() != null && d.getUrgency() >= 3).count());
        s.setEstimatedAffectedUsers(s.getWaterPipelineCount() * 500 + s.getGasPipelineCount() * 300
                + s.getPowerPipelineCount() * 800 + s.getMainRoadCount() * 200);
        return s;
    }

    private int calculateImpactScore(ExcavationImpactAssessmentDTO.ImpactSummary s,
                                     List<PipelineConflict> conflicts) {
        int score = 0;

        if (s.getHighRiskPipelineCount() > 0) score += s.getHighRiskPipelineCount() * 15;
        score += s.getGasPipelineCount() * 10;
        score += s.getPowerPipelineCount() * 8;
        score += s.getWaterPipelineCount() * 4;
        score += s.getTotalPipelines() * 2;

        score += s.getNeedClosureValveCount() * 3;
        score += s.getNeedProtectionManholeCount() * 2;

        score += s.getMainRoadCount() * 10;
        score += s.getTotalRoads() * 3;

        score += s.getUrgentDepartmentCount() * 5;
        score += s.getTotalDepartments() * 2;

        for (PipelineConflict c : conflicts) {
            if (c.getConflictLevel() != null) {
                score += c.getConflictLevel() * 5;
            }
        }

        return Math.min(score, 100);
    }

    private int mapImpactLevel(int score) {
        if (score >= 70) return 4;
        if (score >= 45) return 3;
        if (score >= 20) return 2;
        return 1;
    }

    private String generateComprehensiveOpinion(int impactLevel,
                                                ExcavationImpactAssessmentDTO.ImpactSummary s,
                                                List<PipelineConflict> conflicts,
                                                List<ExcavationImpactAssessmentDTO.NotificationDept> depts) {
        StringBuilder sb = new StringBuilder();
        sb.append("经综合评估，本项目施工影响等级为【").append(IMPACT_LEVEL_NAMES[impactLevel]).append("】，");

        if (impactLevel >= 3) {
            sb.append("影响面较大。");
        } else {
            sb.append("风险可控。");
        }

        if (s.getGasPipelineCount() > 0) {
            sb.append("注意：涉及燃气管线").append(s.getGasPipelineCount()).append("条，");
            if (s.getHighRiskPipelineCount() > 0) {
                sb.append("其中").append(s.getHighRiskPipelineCount()).append("条为高风险，");
            }
            sb.append("必须联系燃气管理单位现场交底后方可施工。");
        }

        if (s.getPowerPipelineCount() > 0) {
            sb.append("涉及电力管线").append(s.getPowerPipelineCount()).append("条，需确认电缆走向及埋深。");
        }

        if (s.getMainRoadCount() > 0) {
            sb.append("影响主干道").append(s.getMainRoadCount()).append("条，建议选择夜间或非高峰时段施工，并做好交通疏导。");
        }

        List<ExcavationImpactAssessmentDTO.NotificationDept> urgentDepts = depts.stream()
                .filter(d -> d.getUrgency() != null && d.getUrgency() >= 3).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(urgentDepts)) {
            sb.append("需立即通知以下部门：");
            urgentDepts.forEach(d -> sb.append(d.getDeptName()).append("、"));
            sb.deleteCharAt(sb.length() - 1).append("。");
        }

        if (impactLevel >= 3) {
            sb.append("综合建议：调整施工方案，缩小开挖范围或采用非开挖技术，重新评估后提交审批。");
        } else {
            sb.append("综合建议：落实管线保护方案，通知相关单位进行现场交底，施工时安排专人监护后方可作业。");
        }

        return sb.toString();
    }

    private String generatePipelineRiskDesc(Pipeline p, double dist) {
        String typeName = p.getPipelineType() != null && p.getPipelineType() < PIPELINE_TYPE_NAMES.length
                ? PIPELINE_TYPE_NAMES[p.getPipelineType()] : "管线";
        if (dist < 2) {
            return typeName + "距离开挖区域仅" + String.format("%.1f", dist) + "米，存在直接破坏风险";
        } else if (dist < 5) {
            return typeName + "位于开挖影响范围内，机械作业可能造成振动损伤";
        } else if (dist < 10) {
            return typeName + "邻近施工区域，需注意堆载和沉降影响";
        }
        return typeName + "距离较远，间接影响较小";
    }

    private String generatePipelineProtection(Pipeline p, double dist) {
        String typeName = p.getPipelineType() != null && p.getPipelineType() < PIPELINE_TYPE_NAMES.length
                ? PIPELINE_TYPE_NAMES[p.getPipelineType()] : "管线";
        if (dist < 2) {
            return "建议人工开挖暴露管线后采用钢板或混凝土进行保护，必要时调整管线位置";
        } else if (dist < 5) {
            return "建议施工前由产权单位现场标记位置，施工时采用人工开挖，严禁机械作业";
        } else if (dist < 10) {
            return "建议施工前确认管线位置，控制施工机械与管线安全距离，设置警示标识";
        }
        return "建议施工前进行管线探测确认位置";
    }

    private String generateTrafficSuggestion(ExcavationImpactAssessmentDTO.AffectedRoadInfo info) {
        int impact = info.getTrafficImpact() != null ? info.getTrafficImpact() : 1;
        return switch (impact) {
            case 4 -> "建议全段封闭施工，制定车辆绕行方案并提前发布交通公告";
            case 3 -> "建议占用半幅车道，设置临时便道，安排专人指挥交通";
            case 2 -> "建议错峰施工，设置临时警示标识，必要时短时分流";
            default -> "正常施工，注意设置施工警示标识";
        };
    }

    private int estimateRoadLevel(String areaName, String roadName) {
        if (StrUtil.isBlank(roadName)) return 3;
        if (roadName.contains("大道") || roadName.contains("街") || roadName.contains("路")
                && (roadName.length() <= 4 || roadName.startsWith("中山") || roadName.startsWith("人民"))) {
            return 1;
        }
        if (roadName.contains("路")) return 2;
        if (roadName.contains("巷") || roadName.contains("弄")) return 4;
        return 3;
    }

    private int estimateLanes(BigDecimal depth) {
        if (depth == null) return 1;
        if (depth.compareTo(new BigDecimal("3")) > 0) return 2;
        return 1;
    }

    private boolean isNearExcavationArea(Pipeline p, ExcavationApplication app, double threshold) {
        return calculateDistanceToCenter(p, app) < threshold;
    }

    private double calculateDistanceToCenter(Pipeline p, ExcavationApplication app) {
        if (p.getStartLng() == null || p.getStartLat() == null
                || app.getCenterLng() == null || app.getCenterLat() == null) {
            return 50;
        }
        double d1 = calculateDistance(p.getStartLng().doubleValue(), p.getStartLat().doubleValue(),
                app.getCenterLng().doubleValue(), app.getCenterLat().doubleValue());
        double d2 = calculateDistance(p.getEndLng() != null ? p.getEndLng().doubleValue() : 0,
                p.getEndLat() != null ? p.getEndLat().doubleValue() : 0,
                app.getCenterLng().doubleValue(), app.getCenterLat().doubleValue());
        return Math.min(d1, d2);
    }

    private double calculateDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null) return 100;
        return calculateDistance(lng1.doubleValue(), lat1.doubleValue(),
                lng2 != null ? lng2.doubleValue() : 0, lat2 != null ? lat2.doubleValue() : 0);
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
