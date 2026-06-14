package com.digitaltwin.pipeline.service.sensor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.dto.linkage.LinkedTaskTriggerDTO;
import com.digitaltwin.pipeline.dto.situation.EmergencyPlanComparisonDTO;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.enums.PipelineTypeEnum;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.service.linkage.LinkedTaskService;
import com.digitaltwin.pipeline.service.sensor.EmergencyPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyPlanServiceImpl implements EmergencyPlanService {

    private final AlarmMapper alarmMapper;
    private final PipelineMapper pipelineMapper;
    private final ValveMapper valveMapper;
    private final LinkedTaskService linkedTaskService;

    public static final int STRATEGY_MIN_USER = 1;
    public static final int STRATEGY_FASTEST = 2;
    public static final int STRATEGY_MIN_RESOURCE = 3;
    public static final int STRATEGY_SAFEST = 4;

    private static final String[] IMPACT_LEVEL_NAMES = {"", "局部影响", "片区影响", "区域影响", "大范围影响"};
    private static final String[] STRATEGY_NAMES = {"", "最小影响用户方案", "最快恢复方案", "最省资源方案", "最安全方案"};

    @Override
    public EmergencyPlanComparisonDTO comparePlans(Long alarmId) {
        Alarm alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }

        Pipeline pipeline = null;
        if (alarm.getPipelineId() != null) {
            pipeline = pipelineMapper.selectById(alarm.getPipelineId());
        }

        List<Valve> sortedValves = getSortedValves(alarm, pipeline);

        EmergencyPlanComparisonDTO result = buildBaseComparison(alarm, pipeline);

        List<EmergencyPlanComparisonDTO.DisposalPlanVO> plans = new ArrayList<>();
        plans.add(buildPlan(alarm, pipeline, sortedValves, STRATEGY_MIN_USER, 1L));
        plans.add(buildPlan(alarm, pipeline, sortedValves, STRATEGY_FASTEST, 2L));
        plans.add(buildPlan(alarm, pipeline, sortedValves, STRATEGY_SAFEST, 3L));
        plans.add(buildPlan(alarm, pipeline, sortedValves, STRATEGY_MIN_RESOURCE, 4L));

        result.setPlans(plans);

        EmergencyPlanComparisonDTO.DisposalPlanVO bestPlan = plans.stream()
                .max(Comparator.comparingInt(p -> p.getScore() != null ? p.getScore().getOverall() : 0))
                .orElse(plans.get(0));
        result.setRecommendedPlanId(bestPlan.getPlanId());
        result.setRecommendationReason(generateRecommendationReason(bestPlan, plans));

        return result;
    }

    @Override
    public EmergencyPlanComparisonDTO.DisposalPlanVO getSinglePlan(Long alarmId, Integer strategyType) {
        Alarm alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }
        if (strategyType == null || strategyType < 1 || strategyType > 4) {
            throw new BusinessException("无效的策略类型");
        }

        Pipeline pipeline = null;
        if (alarm.getPipelineId() != null) {
            pipeline = pipelineMapper.selectById(alarm.getPipelineId());
        }

        List<Valve> sortedValves = getSortedValves(alarm, pipeline);
        return buildPlan(alarm, pipeline, sortedValves, strategyType, strategyType.longValue());
    }

    @Override
    public EmergencyPlanComparisonDTO selectAndExecute(Long alarmId, Long planId, String operatorName) {
        EmergencyPlanComparisonDTO comparison = comparePlans(alarmId);

        EmergencyPlanComparisonDTO.DisposalPlanVO selectedPlan = null;
        if (comparison.getPlans() != null) {
            for (EmergencyPlanComparisonDTO.DisposalPlanVO plan : comparison.getPlans()) {
                if (plan.getPlanId() != null && plan.getPlanId().equals(planId)) {
                    selectedPlan = plan;
                    break;
                }
            }
        }

        if (selectedPlan == null) {
            throw new BusinessException("未找到指定方案");
        }

        try {
            LinkedTaskTriggerDTO triggerDTO = new LinkedTaskTriggerDTO();
            triggerDTO.setSourceType(5);
            triggerDTO.setSourceId(alarmId);
            triggerDTO.setTaskType(2);
            triggerDTO.setTitle("【应急处置】" + (selectedPlan.getPlanName() != null ? selectedPlan.getPlanName() : "告警抢修"));
            triggerDTO.setPriority(4);
            triggerDTO.setDescription(buildTaskDescription(alarmId, selectedPlan, operatorName));
            triggerDTO.setLng(comparison.getLng());
            triggerDTO.setLat(comparison.getLat());
            triggerDTO.setAutoGenerateWorkOrder(1);
            triggerDTO.setAutoNotifyDepartments(1);

            linkedTaskService.triggerFromSource(triggerDTO);
            log.info("方案[{}]已下发联动任务，操作人：{}", planId, operatorName);
        } catch (Exception e) {
            log.warn("自动下发联动任务失败，已记录方案选择：alarmId={}, planId={}, operator={}, error={}",
                    alarmId, planId, operatorName, e.getMessage());
        }

        comparison.setRecommendedPlanId(planId);
        comparison.setRecommendationReason("操作员[" + (StrUtil.isNotBlank(operatorName) ? operatorName : "未知") + "]已选择此方案并下发执行");
        return comparison;
    }

    // ========== 私有辅助方法 ==========

    private EmergencyPlanComparisonDTO buildBaseComparison(Alarm alarm, Pipeline pipeline) {
        EmergencyPlanComparisonDTO result = new EmergencyPlanComparisonDTO();
        result.setAlarmId(alarm.getId());
        result.setPipelineId(alarm.getPipelineId());
        result.setPipelineCode(alarm.getPipelineCode());
        if (pipeline != null) {
            result.setPipelineTypeName(PipelineTypeEnum.getLabel(pipeline.getPipelineType()));
        }
        result.setIncidentTime(alarm.getAlarmTime());
        result.setLng(alarm.getLng());
        result.setLat(alarm.getLat());
        return result;
    }

    private List<Valve> getSortedValves(Alarm alarm, Pipeline pipeline) {
        List<Valve> valves = new ArrayList<>();
        if (pipeline != null && pipeline.getId() != null) {
            valves = valveMapper.selectByPipelineId(pipeline.getId());
        }
        if (CollUtil.isEmpty(valves) && alarm.getLng() != null && alarm.getLat() != null) {
            Integer pType = pipeline != null ? pipeline.getPipelineType() : null;
            valves = valveMapper.selectEmergencyValves(alarm.getLng().doubleValue(), alarm.getLat().doubleValue(), pType, 500.0);
        }
        if (CollUtil.isEmpty(valves)) {
            return valves;
        }

        final BigDecimal lng = alarm.getLng();
        final BigDecimal lat = alarm.getLat();
        valves.sort((v1, v2) -> {
            double d1 = calculateDistance(v1.getLng(), v1.getLat(), lng, lat);
            double d2 = calculateDistance(v2.getLng(), v2.getLat(), lng, lat);
            return Double.compare(d1, d2);
        });
        return valves;
    }

    private EmergencyPlanComparisonDTO.DisposalPlanVO buildPlan(
            Alarm alarm, Pipeline pipeline, List<Valve> sortedValves, int strategyType, Long planId) {

        EmergencyPlanComparisonDTO.DisposalPlanVO plan = new EmergencyPlanComparisonDTO.DisposalPlanVO();
        plan.setPlanId(planId);
        plan.setStrategyType(strategyType);
        plan.setPlanName(STRATEGY_NAMES[strategyType]);

        List<Valve> selectedValves = selectValvesByStrategy(sortedValves, strategyType);
        List<EmergencyPlanComparisonDTO.ValveAction> valveActions = buildValveActions(selectedValves, strategyType);
        plan.setValveActions(valveActions);

        int closeValveCount = (int) valveActions.stream().filter(v -> v.getAction() != null && v.getAction() == 1).count();
        int throttleCount = (int) valveActions.stream().filter(v -> v.getAction() != null && v.getAction() == 2).count();

        plan.setDescription(generatePlanDescription(strategyType, closeValveCount, throttleCount, pipeline));

        EmergencyPlanComparisonDTO.AffectedAreaVO affected = buildAffectedArea(alarm, pipeline, strategyType, closeValveCount);
        plan.setAffected(affected);

        EmergencyPlanComparisonDTO.RecoveryVO recovery = buildRecovery(alarm, pipeline, strategyType, closeValveCount, valveActions);
        plan.setRecovery(recovery);

        EmergencyPlanComparisonDTO.ResourceVO resources = buildResources(strategyType, closeValveCount, pipeline);
        plan.setResources(resources);

        EmergencyPlanComparisonDTO.RiskVO risk = buildRisk(strategyType, closeValveCount, pipeline, alarm);
        plan.setRisk(risk);

        EmergencyPlanComparisonDTO.ScoreVO score = calculateScore(affected, recovery, resources, risk, strategyType);
        plan.setScore(score);

        return plan;
    }

    private List<Valve> selectValvesByStrategy(List<Valve> sortedValves, int strategyType) {
        if (CollUtil.isEmpty(sortedValves)) {
            return Collections.emptyList();
        }
        int count;
        switch (strategyType) {
            case STRATEGY_FASTEST:
                count = Math.min(2, sortedValves.size());
                break;
            case STRATEGY_MIN_USER:
                count = Math.min(3, sortedValves.size());
                break;
            case STRATEGY_MIN_RESOURCE:
                count = Math.min(2, sortedValves.size());
                break;
            case STRATEGY_SAFEST:
            default:
                count = Math.min(5, sortedValves.size());
                break;
        }
        return new ArrayList<>(sortedValves.subList(0, count));
    }

    private List<EmergencyPlanComparisonDTO.ValveAction> buildValveActions(List<Valve> valves, int strategyType) {
        List<EmergencyPlanComparisonDTO.ValveAction> result = new ArrayList<>();
        int orderNo = 1;
        for (int i = 0; i < valves.size(); i++) {
            Valve v = valves.get(i);
            EmergencyPlanComparisonDTO.ValveAction action = new EmergencyPlanComparisonDTO.ValveAction();
            action.setOrderNo(orderNo++);
            action.setValveId(v.getId());
            action.setValveCode(v.getValveCode());
            action.setValveName(v.getValveName());
            action.setRemoteControllable(v.getRemoteControllable());

            if (strategyType == STRATEGY_SAFEST) {
                action.setAction(1);
            } else if (strategyType == STRATEGY_MIN_RESOURCE && i >= 2) {
                action.setAction(0);
            } else if (strategyType == STRATEGY_MIN_USER && i >= 2) {
                action.setAction(2);
            } else if (strategyType == STRATEGY_FASTEST) {
                action.setAction(i < 2 ? 1 : 0);
            } else {
                action.setAction(i < 2 ? 1 : 2);
            }

            int baseMin = (action.getRemoteControllable() != null && action.getRemoteControllable() == 1) ? 2 : 10;
            if (action.getAction() != null && action.getAction() == 2) {
                baseMin += 3;
            } else if (action.getAction() != null && action.getAction() == 0) {
                baseMin = 0;
            }
            action.setOperationMinutes(baseMin + new Random().nextInt(5));
            result.add(action);
        }
        return result;
    }

    private String generatePlanDescription(int strategyType, int closeCount, int throttleCount, Pipeline pipeline) {
        String pType = pipeline != null ? PipelineTypeEnum.getLabel(pipeline.getPipelineType()) : "管线";
        StringBuilder sb = new StringBuilder();
        switch (strategyType) {
            case STRATEGY_FASTEST:
                sb.append(String.format("仅关闭最邻近%d个阀门，快速隔离故障点，", closeCount));
                sb.append("最大限度缩短恢复时间，但隔离范围较小，存在次生泄漏风险");
                break;
            case STRATEGY_MIN_USER:
                sb.append(String.format("关闭%d个阀门、节流%d个阀门，", closeCount, throttleCount));
                sb.append("精准控制隔离范围，将受影响用户降至最低");
                break;
            case STRATEGY_MIN_RESOURCE:
                sb.append(String.format("仅关%d个核心阀门，", closeCount));
                sb.append("最小化人力和设备投入，降低处置成本");
                break;
            case STRATEGY_SAFEST:
            default:
                sb.append(String.format("关闭%d个冗余阀门构建多重隔离带，", closeCount));
                sb.append("彻底杜绝次生风险，但影响范围较大、恢复时间较长");
                break;
        }
        return sb.toString();
    }

    private EmergencyPlanComparisonDTO.AffectedAreaVO buildAffectedArea(
            Alarm alarm, Pipeline pipeline, int strategyType, int closeValveCount) {

        EmergencyPlanComparisonDTO.AffectedAreaVO area = new EmergencyPlanComparisonDTO.AffectedAreaVO();

        int impactLevel;
        double radius;
        switch (strategyType) {
            case STRATEGY_FASTEST:
                impactLevel = 1;
                radius = 80;
                break;
            case STRATEGY_MIN_USER:
                impactLevel = 2;
                radius = 150;
                break;
            case STRATEGY_MIN_RESOURCE:
                impactLevel = 2;
                radius = 180;
                break;
            case STRATEGY_SAFEST:
            default:
                impactLevel = Math.min(4, 2 + Math.max(0, closeValveCount - 2));
                radius = 150 + closeValveCount * 80;
                break;
        }

        if (pipeline != null) {
            if (pipeline.getPipelineType() != null && (pipeline.getPipelineType() == 3 || pipeline.getPipelineType() == 4)) {
                impactLevel = Math.min(impactLevel + 1, 4);
                radius *= 1.3;
            }
            if (pipeline.getDiameter() != null && pipeline.getDiameter().compareTo(new BigDecimal("500")) > 0) {
                impactLevel = Math.min(impactLevel + 1, 4);
                radius *= 1.2;
            }
        }

        area.setImpactLevel(impactLevel);
        area.setImpactLevelName(IMPACT_LEVEL_NAMES[impactLevel]);
        area.setRadius(BigDecimal.valueOf(radius).setScale(0, RoundingMode.HALF_UP));

        int baseUsers = impactLevel * 150;
        if (pipeline != null && pipeline.getPipelineType() != null) {
            if (pipeline.getPipelineType() == 1) baseUsers *= 2;
            if (pipeline.getPipelineType() == 4) baseUsers *= 3;
        }
        if (strategyType == STRATEGY_MIN_USER) baseUsers = (int) (baseUsers * 0.5);
        if (strategyType == STRATEGY_FASTEST) baseUsers = (int) (baseUsers * 0.6);
        if (strategyType == STRATEGY_SAFEST) baseUsers = (int) (baseUsers * 1.5);
        area.setAffectedUsers(Math.max(10, baseUsers + new Random().nextInt(Math.max(1, baseUsers / 2))));
        area.setAffectedEnterprises(impactLevel * 5 + new Random().nextInt(15));

        List<String> roads = new ArrayList<>();
        String roadName = pipeline != null ? pipeline.getRoadName() : null;
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

    private EmergencyPlanComparisonDTO.RecoveryVO buildRecovery(
            Alarm alarm, Pipeline pipeline, int strategyType, int closeValveCount,
            List<EmergencyPlanComparisonDTO.ValveAction> valveActions) {

        EmergencyPlanComparisonDTO.RecoveryVO recovery = new EmergencyPlanComparisonDTO.RecoveryVO();

        int operationMinutes = valveActions.stream()
                .filter(v -> v.getAction() != null && v.getAction() != 0)
                .mapToInt(v -> v.getOperationMinutes() != null ? v.getOperationMinutes() : 0)
                .sum();
        int travelMinutes = 15 + new Random().nextInt(15);
        int totalIsolationMinutes = travelMinutes + operationMinutes + 5;
        recovery.setTotalIsolationMinutes(totalIsolationMinutes);

        int baseRepairMinutes = 60;
        if (pipeline != null && pipeline.getDiameter() != null) {
            if (pipeline.getDiameter().compareTo(new BigDecimal("300")) > 0) baseRepairMinutes += 30;
            if (pipeline.getDiameter().compareTo(new BigDecimal("600")) > 0) baseRepairMinutes += 60;
        }
        if (alarm.getAlarmLevel() != null) {
            baseRepairMinutes += alarm.getAlarmLevel() * 15;
        }
        if (strategyType == STRATEGY_SAFEST) baseRepairMinutes += 30;
        recovery.setTotalRepairMinutes(baseRepairMinutes + new Random().nextInt(30));

        int recoveryMinutes;
        switch (strategyType) {
            case STRATEGY_FASTEST:
                recoveryMinutes = totalIsolationMinutes + recovery.getTotalRepairMinutes() + 20;
                break;
            case STRATEGY_SAFEST:
                recoveryMinutes = totalIsolationMinutes + recovery.getTotalRepairMinutes() + 80 + closeValveCount * 10;
                break;
            default:
                recoveryMinutes = totalIsolationMinutes + recovery.getTotalRepairMinutes() + 45;
                break;
        }
        recovery.setTotalRecoveryMinutes(recoveryMinutes);

        List<EmergencyPlanComparisonDTO.RecoveryStepVO> timeline = new ArrayList<>();
        int offset = 0;

        EmergencyPlanComparisonDTO.RecoveryStepVO s1 = new EmergencyPlanComparisonDTO.RecoveryStepVO();
        s1.setTimeOffsetMinutes(offset);
        s1.setStepName("赶赴现场");
        s1.setDescription("运维人员携带设备赶赴告警位置");
        timeline.add(s1);
        offset += travelMinutes;

        EmergencyPlanComparisonDTO.RecoveryStepVO s2 = new EmergencyPlanComparisonDTO.RecoveryStepVO();
        s2.setTimeOffsetMinutes(offset);
        s2.setStepName("关阀隔离");
        s2.setDescription(String.format("按顺序操作%d个阀门完成故障段隔离", closeValveCount));
        timeline.add(s2);
        offset += operationMinutes + 5;

        EmergencyPlanComparisonDTO.RecoveryStepVO s3 = new EmergencyPlanComparisonDTO.RecoveryStepVO();
        s3.setTimeOffsetMinutes(offset);
        s3.setStepName("故障排查");
        s3.setDescription("现场排查确认故障点位置和损坏程度");
        timeline.add(s3);
        offset += 20;

        EmergencyPlanComparisonDTO.RecoveryStepVO s4 = new EmergencyPlanComparisonDTO.RecoveryStepVO();
        s4.setTimeOffsetMinutes(offset);
        s4.setStepName("维修作业");
        s4.setDescription("实施维修/更换/堵漏等处置作业");
        timeline.add(s4);
        offset += recovery.getTotalRepairMinutes() - 20;

        EmergencyPlanComparisonDTO.RecoveryStepVO s5 = new EmergencyPlanComparisonDTO.RecoveryStepVO();
        s5.setTimeOffsetMinutes(offset);
        s5.setStepName("恢复验证");
        s5.setDescription("开启阀门，监测压力/流量恢复正常");
        timeline.add(s5);
        offset += (strategyType == STRATEGY_SAFEST ? 60 : 30);

        EmergencyPlanComparisonDTO.RecoveryStepVO s6 = new EmergencyPlanComparisonDTO.RecoveryStepVO();
        s6.setTimeOffsetMinutes(offset);
        s6.setStepName("收尾归档");
        s6.setDescription("记录归档，关闭工单");
        timeline.add(s6);

        recovery.setRecoveryTimeline(timeline);
        return recovery;
    }

    private EmergencyPlanComparisonDTO.ResourceVO buildResources(int strategyType, int closeValveCount, Pipeline pipeline) {
        EmergencyPlanComparisonDTO.ResourceVO resource = new EmergencyPlanComparisonDTO.ResourceVO();

        switch (strategyType) {
            case STRATEGY_MIN_RESOURCE:
                resource.setRequiredWorkers(2);
                resource.setRequiredVehicles(1);
                break;
            case STRATEGY_FASTEST:
                resource.setRequiredWorkers(3);
                resource.setRequiredVehicles(2);
                break;
            case STRATEGY_MIN_USER:
                resource.setRequiredWorkers(4);
                resource.setRequiredVehicles(2);
                break;
            case STRATEGY_SAFEST:
            default:
                resource.setRequiredWorkers(5 + Math.max(0, closeValveCount - 2));
                resource.setRequiredVehicles(3);
                break;
        }

        List<String> materials = new ArrayList<>();
        materials.add("应急抢修工具包");
        if (pipeline != null) {
            if (pipeline.getPipelineType() != null && pipeline.getPipelineType() == 3) {
                materials.add("可燃气体检测仪");
                materials.add("防爆工具");
            }
            if (pipeline.getPipelineType() != null && (pipeline.getPipelineType() == 1 || pipeline.getPipelineType() == 2)) {
                materials.add("堵漏器材");
                materials.add("水泵");
            }
        }
        if (strategyType == STRATEGY_SAFEST) {
            materials.add("安全警示带");
            materials.add("临时警戒设施");
        }
        if (closeValveCount >= 4) {
            materials.add("便携式阀门操作设备");
        }
        resource.setRequiredMaterials(materials);
        return resource;
    }

    private EmergencyPlanComparisonDTO.RiskVO buildRisk(int strategyType, int closeValveCount, Pipeline pipeline, Alarm alarm) {
        EmergencyPlanComparisonDTO.RiskVO risk = new EmergencyPlanComparisonDTO.RiskVO();

        int safetyRisk;
        int operationRisk;
        int secondaryRisk;

        switch (strategyType) {
            case STRATEGY_SAFEST:
                safetyRisk = 1;
                operationRisk = Math.min(4, 2 + closeValveCount / 3);
                secondaryRisk = 1;
                break;
            case STRATEGY_FASTEST:
                safetyRisk = 3;
                operationRisk = 1;
                secondaryRisk = 3;
                break;
            case STRATEGY_MIN_USER:
                safetyRisk = 2;
                operationRisk = 2;
                secondaryRisk = 2;
                break;
            case STRATEGY_MIN_RESOURCE:
            default:
                safetyRisk = 3;
                operationRisk = 2;
                secondaryRisk = 3;
                break;
        }

        if (pipeline != null && pipeline.getPipelineType() != null && pipeline.getPipelineType() == 3) {
            safetyRisk = Math.min(4, safetyRisk + 1);
            secondaryRisk = Math.min(4, secondaryRisk + 1);
        }
        if (alarm.getAlarmLevel() != null) {
            safetyRisk = Math.min(4, Math.max(1, safetyRisk + (alarm.getAlarmLevel() - 2)));
        }

        risk.setSafetyRisk(safetyRisk);
        risk.setOperationRisk(operationRisk);
        risk.setSecondaryRisk(secondaryRisk);

        List<String> riskItems = new ArrayList<>();
        if (safetyRisk >= 3) riskItems.add("安全风险较高，需加强现场监护");
        if (safetyRisk >= 4) riskItems.add("极高安全风险，建议升级指挥级别");
        if (operationRisk >= 3) riskItems.add("操作步骤多，需安排熟练人员执行");
        if (secondaryRisk >= 3) riskItems.add("存在次生泄漏/扩散风险，需持续监测");
        if (strategyType == STRATEGY_FASTEST) riskItems.add("隔离范围有限，故障可能扩大");
        if (strategyType == STRATEGY_SAFEST) riskItems.add("大范围停供，用户投诉风险");
        risk.setRiskItems(riskItems);
        return risk;
    }

    private EmergencyPlanComparisonDTO.ScoreVO calculateScore(
            EmergencyPlanComparisonDTO.AffectedAreaVO affected,
            EmergencyPlanComparisonDTO.RecoveryVO recovery,
            EmergencyPlanComparisonDTO.ResourceVO resources,
            EmergencyPlanComparisonDTO.RiskVO risk,
            int strategyType) {

        EmergencyPlanComparisonDTO.ScoreVO score = new EmergencyPlanComparisonDTO.ScoreVO();

        int userImpactScore = 100 - (affected.getImpactLevel() != null ? (affected.getImpactLevel() - 1) * 20 : 0);
        if (affected.getAffectedUsers() != null) {
            userImpactScore = Math.max(0, Math.min(100, userImpactScore - affected.getAffectedUsers() / 50));
        }
        if (strategyType == STRATEGY_MIN_USER) userImpactScore = Math.min(100, userImpactScore + 15);
        score.setUserImpactScore(userImpactScore);

        int totalMinutes = recovery.getTotalRecoveryMinutes() != null ? recovery.getTotalRecoveryMinutes() : 120;
        int recoveryTimeScore = Math.max(0, Math.min(100, 100 - totalMinutes / 4));
        if (strategyType == STRATEGY_FASTEST) recoveryTimeScore = Math.min(100, recoveryTimeScore + 15);
        score.setRecoveryTimeScore(recoveryTimeScore);

        int baseResourceScore = 100;
        if (resources.getRequiredWorkers() != null) baseResourceScore -= resources.getRequiredWorkers() * 5;
        if (resources.getRequiredVehicles() != null) baseResourceScore -= resources.getRequiredVehicles() * 5;
        if (resources.getRequiredMaterials() != null) baseResourceScore -= resources.getRequiredMaterials().size() * 3;
        int resourceScore = Math.max(0, Math.min(100, baseResourceScore));
        if (strategyType == STRATEGY_MIN_RESOURCE) resourceScore = Math.min(100, resourceScore + 15);
        score.setResourceScore(resourceScore);

        int safetyScore = 100;
        if (risk.getSafetyRisk() != null) safetyScore -= (risk.getSafetyRisk() - 1) * 20;
        if (risk.getOperationRisk() != null) safetyScore -= (risk.getOperationRisk() - 1) * 5;
        if (risk.getSecondaryRisk() != null) safetyScore -= (risk.getSecondaryRisk() - 1) * 10;
        safetyScore = Math.max(0, Math.min(100, safetyScore));
        if (strategyType == STRATEGY_SAFEST) safetyScore = Math.min(100, safetyScore + 15);
        score.setSafetyScore(safetyScore);

        int overall = (int) (safetyScore * 0.35 + recoveryTimeScore * 0.25 + userImpactScore * 0.25 + resourceScore * 0.15);
        score.setOverall(Math.max(0, Math.min(100, overall)));

        List<String> pros = new ArrayList<>();
        List<String> cons = new ArrayList<>();
        switch (strategyType) {
            case STRATEGY_FASTEST:
                pros.add("恢复速度最快");
                pros.add("操作步骤简单");
                cons.add("安全系数较低");
                cons.add("存在次生风险");
                break;
            case STRATEGY_MIN_USER:
                pros.add("影响用户最少");
                pros.add("投诉风险低");
                cons.add("关阀策略较复杂");
                cons.add("恢复时间中等");
                break;
            case STRATEGY_MIN_RESOURCE:
                pros.add("人力物力投入最少");
                pros.add("处置成本最低");
                cons.add("安全保障一般");
                cons.add("影响范围较大");
                break;
            case STRATEGY_SAFEST:
            default:
                pros.add("安全系数最高");
                pros.add("彻底杜绝次生风险");
                cons.add("影响范围最大");
                cons.add("恢复时间最长");
                cons.add("资源投入最多");
                break;
        }
        score.setPros(pros);
        score.setCons(cons);
        return score;
    }

    private String generateRecommendationReason(EmergencyPlanComparisonDTO.DisposalPlanVO bestPlan,
                                                 List<EmergencyPlanComparisonDTO.DisposalPlanVO> allPlans) {
        StringBuilder sb = new StringBuilder();
        sb.append("综合评分").append(bestPlan.getScore() != null ? bestPlan.getScore().getOverall() : 0).append("分，");
        sb.append("在所有方案中最高。");

        if (bestPlan.getScore() != null) {
            List<String> highlights = new ArrayList<>();
            if (bestPlan.getScore().getSafetyScore() != null && bestPlan.getScore().getSafetyScore() >= 70) {
                highlights.add("安全性表现突出");
            }
            if (bestPlan.getScore().getRecoveryTimeScore() != null && bestPlan.getScore().getRecoveryTimeScore() >= 70) {
                highlights.add("恢复速度快");
            }
            if (bestPlan.getScore().getUserImpactScore() != null && bestPlan.getScore().getUserImpactScore() >= 70) {
                highlights.add("用户影响小");
            }
            if (bestPlan.getScore().getResourceScore() != null && bestPlan.getScore().getResourceScore() >= 70) {
                highlights.add("资源消耗低");
            }
            if (!highlights.isEmpty()) {
                sb.append(String.join("、", highlights)).append("。");
            }
        }

        sb.append("建议优先采用此方案。");
        return sb.toString();
    }

    private String buildTaskDescription(Long alarmId, EmergencyPlanComparisonDTO.DisposalPlanVO plan, String operatorName) {
        StringBuilder sb = new StringBuilder();
        sb.append("告警ID:").append(alarmId).append(" | ");
        sb.append("方案:").append(plan.getPlanName()).append(" | ");
        if (plan.getValveActions() != null) {
            long closeCount = plan.getValveActions().stream().filter(v -> v.getAction() != null && v.getAction() == 1).count();
            sb.append("关阀数量:").append(closeCount).append(" | ");
        }
        if (plan.getRecovery() != null) {
            sb.append("预计恢复:").append(plan.getRecovery().getTotalRecoveryMinutes()).append("分钟 | ");
        }
        if (StrUtil.isNotBlank(operatorName)) {
            sb.append("选定人:").append(operatorName);
        }
        return sb.toString();
    }

    private double calculateDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) return 100;
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
