package com.digitaltwin.pipeline.service.situation.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.situation.EmergencyPlanComparisonDTO;
import com.digitaltwin.pipeline.dto.situation.PlanExecutionDetailVO;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.situation.PlanExecutionRecord;
import com.digitaltwin.pipeline.entity.situation.PlanExecutionTimeline;
import com.digitaltwin.pipeline.entity.situation.PlanReplayAnalysis;
import com.digitaltwin.pipeline.entity.situation.ValveOperationRecord;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.situation.PlanExecutionRecordMapper;
import com.digitaltwin.pipeline.mapper.situation.PlanExecutionTimelineMapper;
import com.digitaltwin.pipeline.mapper.situation.PlanReplayAnalysisMapper;
import com.digitaltwin.pipeline.mapper.situation.ValveOperationRecordMapper;
import com.digitaltwin.pipeline.service.sensor.EmergencyPlanService;
import com.digitaltwin.pipeline.service.situation.PlanExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanExecutionServiceImpl implements PlanExecutionService {

    private final PlanExecutionRecordMapper executionRecordMapper;
    private final ValveOperationRecordMapper valveOperationMapper;
    private final PlanExecutionTimelineMapper timelineMapper;
    private final PlanReplayAnalysisMapper replayAnalysisMapper;
    private final AlarmMapper alarmMapper;
    private final EmergencyPlanService emergencyPlanService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String[] STRATEGY_NAMES = {"", "最小影响用户方案", "最快恢复方案", "最省资源方案", "最安全方案"};
    private static final String[] STATUS_NAMES = {"", "待执行", "执行中", "已完成", "部分成功", "执行失败"};
    private static final String[] ACTION_NAMES = {"不动", "关阀", "节流"};
    private static final String[] POINT_TYPE_NAMES = {"", "方案选定", "派单", "到达现场", "开始关阀", "完成关阀",
            "开始维修", "完成维修", "开始恢复", "恢复完成", "意外事件"};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanExecutionRecord startExecution(Long alarmId, Long planId, Integer strategyType, String operatorName) {
        Alarm alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }

        EmergencyPlanComparisonDTO.DisposalPlanVO plan = emergencyPlanService.getSinglePlan(alarmId, strategyType);
        if (plan == null) {
            throw new BusinessException("未找到指定方案");
        }

        PlanExecutionRecord record = new PlanExecutionRecord();
        record.setRecordCode(generateRecordCode());
        record.setAlarmId(alarmId);
        record.setAlarmCode(alarm.getAlarmCode());
        record.setPlanId(planId);
        record.setPlanName(plan.getPlanName() != null ? plan.getPlanName() : STRATEGY_NAMES[strategyType]);
        record.setStrategyType(strategyType);
        record.setStatus(1);
        record.setOperatorName(operatorName);
        record.setStartTime(LocalDateTime.now());

        if (plan.getRecovery() != null && plan.getRecovery().getTotalRecoveryMinutes() != null) {
            record.setEstimatedTotalMinutes(plan.getRecovery().getTotalRecoveryMinutes());
        } else {
            record.setEstimatedTotalMinutes(120);
        }

        if (plan.getAffected() != null) {
            record.setEstimatedAffectedUsers(plan.getAffected().getAffectedUsers());
            record.setEstimatedAffectedEnterprises(plan.getAffected().getAffectedEnterprises());
        } else {
            record.setEstimatedAffectedUsers(100);
            record.setEstimatedAffectedEnterprises(5);
        }

        executionRecordMapper.insert(record);

        if (plan.getValveActions() != null && !plan.getValveActions().isEmpty()) {
            int orderNo = 1;
            for (EmergencyPlanComparisonDTO.ValveAction va : plan.getValveActions()) {
                ValveOperationRecord vor = new ValveOperationRecord();
                vor.setExecutionId(record.getId());
                vor.setExecutionCode(record.getRecordCode());
                vor.setValveId(va.getValveId());
                vor.setValveCode(va.getValveCode());
                vor.setValveName(va.getValveName());
                vor.setPlannedAction(va.getAction());
                vor.setPlannedOrderNo(orderNo++);
                vor.setPlannedMinutes(va.getOperationMinutes());
                vor.setRemoteControllable(va.getRemoteControllable());
                valveOperationMapper.insert(vor);
            }
        }

        PlanExecutionTimeline timeline = new PlanExecutionTimeline();
        timeline.setExecutionId(record.getId());
        timeline.setExecutionCode(record.getRecordCode());
        timeline.setPointType(1);
        timeline.setTitle("方案选定");
        timeline.setDescription("操作员[" + operatorName + "]选定" + (plan.getPlanName() != null ? plan.getPlanName() : STRATEGY_NAMES[strategyType]) + "，开始执行");
        timeline.setOccurTime(LocalDateTime.now());
        timeline.setOperatorName(operatorName);
        timeline.setIsKeyNode(1);
        timelineMapper.insert(timeline);

        log.info("方案执行已启动，执行记录ID：{}，告警ID：{}，操作人：{}", record.getId(), alarmId, operatorName);
        return record;
    }

    @Override
    public PlanExecutionDetailVO getExecutionDetail(Long executionId) {
        PlanExecutionRecord record = executionRecordMapper.selectById(executionId);
        if (record == null) {
            throw new BusinessException("执行记录不存在");
        }

        PlanExecutionDetailVO vo = new PlanExecutionDetailVO();
        vo.setId(record.getId());
        vo.setRecordCode(record.getRecordCode());
        vo.setAlarmId(record.getAlarmId());
        vo.setAlarmCode(record.getAlarmCode());
        vo.setEventId(record.getEventId());
        vo.setEventCode(record.getEventCode());
        vo.setPlanId(record.getPlanId());
        vo.setPlanName(record.getPlanName());
        vo.setStrategyType(record.getStrategyType());
        vo.setStrategyTypeName(record.getStrategyType() != null && record.getStrategyType() > 0
                && record.getStrategyType() < STRATEGY_NAMES.length ? STRATEGY_NAMES[record.getStrategyType()] : "");
        vo.setStatus(record.getStatus());
        vo.setStatusName(record.getStatus() != null && record.getStatus() > 0
                && record.getStatus() < STATUS_NAMES.length ? STATUS_NAMES[record.getStatus()] : "");
        vo.setExecutor(record.getExecutor());
        vo.setExecutorDept(record.getExecutorDept());
        vo.setOperatorName(record.getOperatorName());
        vo.setStartTime(record.getStartTime());
        vo.setEndTime(record.getEndTime());
        vo.setTotalUsedMinutes(record.getTotalUsedMinutes());
        vo.setEstimatedTotalMinutes(record.getEstimatedTotalMinutes());
        vo.setActualAffectedUsers(record.getActualAffectedUsers());
        vo.setEstimatedAffectedUsers(record.getEstimatedAffectedUsers());
        vo.setActualAffectedEnterprises(record.getActualAffectedEnterprises());
        vo.setEstimatedAffectedEnterprises(record.getEstimatedAffectedEnterprises());
        vo.setDeviationMinutes(record.getDeviationMinutes());
        vo.setDeviationUsers(record.getDeviationUsers());
        vo.setAccuracyScore(record.getAccuracyScore());
        vo.setRemark(record.getRemark());

        List<ValveOperationRecord> valveRecords = valveOperationMapper.selectList(
                new LambdaQueryWrapper<ValveOperationRecord>()
                        .eq(ValveOperationRecord::getExecutionId, executionId)
                        .orderByAsc(ValveOperationRecord::getPlannedOrderNo)
        );
        List<PlanExecutionDetailVO.ValveOperationVO> valveVOS = new ArrayList<>();
        for (ValveOperationRecord vor : valveRecords) {
            PlanExecutionDetailVO.ValveOperationVO vvo = new PlanExecutionDetailVO.ValveOperationVO();
            vvo.setId(vor.getId());
            vvo.setValveId(vor.getValveId());
            vvo.setValveCode(vor.getValveCode());
            vvo.setValveName(vor.getValveName());
            vvo.setPlannedAction(vor.getPlannedAction());
            vvo.setPlannedActionName(vor.getPlannedAction() != null && vor.getPlannedAction() >= 0
                    && vor.getPlannedAction() < ACTION_NAMES.length ? ACTION_NAMES[vor.getPlannedAction()] : "");
            vvo.setActualAction(vor.getActualAction());
            vvo.setActualActionName(vor.getActualAction() != null && vor.getActualAction() >= 0
                    && vor.getActualAction() < ACTION_NAMES.length ? ACTION_NAMES[vor.getActualAction()] : "");
            vvo.setPlannedOrderNo(vor.getPlannedOrderNo());
            vvo.setActualOrderNo(vor.getActualOrderNo());
            vvo.setPlannedMinutes(vor.getPlannedMinutes());
            vvo.setActualMinutes(vor.getActualMinutes());
            if (vor.getPlannedMinutes() != null && vor.getActualMinutes() != null) {
                vvo.setTimeDeviation(vor.getActualMinutes() - vor.getPlannedMinutes());
            }
            vvo.setOperatorName(vor.getOperatorName());
            vvo.setOperationTime(vor.getOperationTime());
            vvo.setRemoteControllable(vor.getRemoteControllable());
            vvo.setActualIsRemote(vor.getActualIsRemote());
            vvo.setIsSuccessful(vor.getIsSuccessful());
            vvo.setIsSuccessfulName(vor.getIsSuccessful() != null && vor.getIsSuccessful() == 1 ? "成功" : "失败");
            vvo.setFailReason(vor.getFailReason());
            vvo.setBeforeStatus(vor.getBeforeStatus());
            vvo.setAfterStatus(vor.getAfterStatus());
            if (vor.getPlannedAction() != null && vor.getActualAction() != null) {
                vvo.setActionConsistent(vor.getPlannedAction().equals(vor.getActualAction()) ? 1 : 0);
            }
            vvo.setRemark(vor.getRemark());
            valveVOS.add(vvo);
        }
        vo.setValveOperations(valveVOS);

        List<PlanExecutionTimeline> timelineList = timelineMapper.selectList(
                new LambdaQueryWrapper<PlanExecutionTimeline>()
                        .eq(PlanExecutionTimeline::getExecutionId, executionId)
                        .orderByAsc(PlanExecutionTimeline::getOccurTime)
        );
        List<PlanExecutionDetailVO.TimelineItemVO> timelineVOS = new ArrayList<>();
        for (PlanExecutionTimeline t : timelineList) {
            PlanExecutionDetailVO.TimelineItemVO tvo = new PlanExecutionDetailVO.TimelineItemVO();
            tvo.setId(t.getId());
            tvo.setPointType(t.getPointType());
            tvo.setPointTypeName(t.getPointType() != null && t.getPointType() > 0
                    && t.getPointType() < POINT_TYPE_NAMES.length ? POINT_TYPE_NAMES[t.getPointType()] : "");
            tvo.setTitle(t.getTitle());
            tvo.setDescription(t.getDescription());
            tvo.setOccurTime(t.getOccurTime());
            tvo.setOperatorName(t.getOperatorName());
            tvo.setIsKeyNode(t.getIsKeyNode());
            tvo.setResourceType(t.getResourceType());
            tvo.setResourceId(t.getResourceId());
            tvo.setResourceCode(t.getResourceCode());
            timelineVOS.add(tvo);
        }
        vo.setTimeline(timelineVOS);

        PlanExecutionDetailVO.ExecutionComparisonVO comparison = buildComparison(record, valveRecords);
        vo.setComparison(comparison);

        PlanReplayAnalysis analysis = replayAnalysisMapper.selectOne(
                new LambdaQueryWrapper<PlanReplayAnalysis>()
                        .eq(PlanReplayAnalysis::getExecutionId, executionId)
                        .last("LIMIT 1")
        );
        if (analysis != null) {
            PlanExecutionDetailVO.ReplayAnalysisVO ravo = new PlanExecutionDetailVO.ReplayAnalysisVO();
            ravo.setId(analysis.getId());
            ravo.setPlanAccuracyScore(analysis.getPlanAccuracyScore());
            ravo.setTimeDeviationScore(analysis.getTimeDeviationScore());
            ravo.setImpactDeviationScore(analysis.getImpactDeviationScore());
            ravo.setOperationAccuracyScore(analysis.getOperationAccuracyScore());
            int overall = 0;
            int count = 0;
            if (analysis.getPlanAccuracyScore() != null) { overall += analysis.getPlanAccuracyScore(); count++; }
            if (analysis.getTimeDeviationScore() != null) { overall += analysis.getTimeDeviationScore(); count++; }
            if (analysis.getImpactDeviationScore() != null) { overall += analysis.getImpactDeviationScore(); count++; }
            if (analysis.getOperationAccuracyScore() != null) { overall += analysis.getOperationAccuracyScore(); count++; }
            ravo.setOverallScore(count > 0 ? overall / count : 0);
            ravo.setGoodPoints(analysis.getGoodPoints());
            ravo.setImprovementPoints(analysis.getImprovementPoints());
            ravo.setSuggestions(analysis.getSuggestions());
            ravo.setLearnedLessons(analysis.getLearnedLessons());
            ravo.setReviewerName(analysis.getReviewerName());
            ravo.setReviewTime(analysis.getReviewTime());
            vo.setAnalysis(ravo);
        }

        vo.setDeviationDescription(buildDeviationDescription(record, comparison));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ValveOperationRecord recordValveOperation(Long executionId, ValveOperationRecord record) {
        PlanExecutionRecord execution = executionRecordMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }

        if (record.getId() != null) {
            ValveOperationRecord existing = valveOperationMapper.selectById(record.getId());
            if (existing != null) {
                record.setExecutionId(executionId);
                record.setExecutionCode(execution.getRecordCode());
                valveOperationMapper.updateById(record);
            }
        } else {
            record.setExecutionId(executionId);
            record.setExecutionCode(execution.getRecordCode());
            valveOperationMapper.insert(record);
        }

        if (execution.getStatus() == null || execution.getStatus() == 1) {
            execution.setStatus(2);
            executionRecordMapper.updateById(execution);
        }

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanExecutionTimeline addTimelinePoint(Long executionId, PlanExecutionTimeline point) {
        PlanExecutionRecord execution = executionRecordMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }

        point.setExecutionId(executionId);
        point.setExecutionCode(execution.getRecordCode());
        if (point.getOccurTime() == null) {
            point.setOccurTime(LocalDateTime.now());
        }
        timelineMapper.insert(point);

        return point;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanExecutionRecord completeExecution(Long executionId, String result, String operatorName) {
        PlanExecutionRecord execution = executionRecordMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }

        execution.setEndTime(LocalDateTime.now());
        if (execution.getStartTime() != null) {
            long minutes = Duration.between(execution.getStartTime(), execution.getEndTime()).toMinutes();
            execution.setTotalUsedMinutes((int) minutes);
        }

        if (execution.getEstimatedTotalMinutes() != null && execution.getTotalUsedMinutes() != null) {
            execution.setDeviationMinutes(execution.getTotalUsedMinutes() - execution.getEstimatedTotalMinutes());
        }

        if (execution.getEstimatedAffectedUsers() != null && execution.getActualAffectedUsers() != null) {
            execution.setDeviationUsers(execution.getActualAffectedUsers() - execution.getEstimatedAffectedUsers());
        }

        List<ValveOperationRecord> valveRecords = valveOperationMapper.selectList(
                new LambdaQueryWrapper<ValveOperationRecord>()
                        .eq(ValveOperationRecord::getExecutionId, executionId)
        );

        int accuracyScore = calculateAccuracyScore(execution, valveRecords);
        execution.setAccuracyScore(accuracyScore);

        int status;
        if ("success".equals(result)) {
            status = 3;
        } else if ("partial".equals(result)) {
            status = 4;
        } else if ("failed".equals(result)) {
            status = 5;
        } else {
            status = 3;
        }
        execution.setStatus(status);
        execution.setOperatorName(operatorName);

        executionRecordMapper.updateById(execution);

        PlanExecutionTimeline timeline = new PlanExecutionTimeline();
        timeline.setExecutionId(executionId);
        timeline.setExecutionCode(execution.getRecordCode());
        timeline.setPointType(9);
        timeline.setTitle("恢复完成");
        timeline.setDescription("执行完成，结果：" + STATUS_NAMES[status] + "，准确度评分：" + accuracyScore + "分");
        timeline.setOccurTime(LocalDateTime.now());
        timeline.setOperatorName(operatorName);
        timeline.setIsKeyNode(1);
        timelineMapper.insert(timeline);

        log.info("方案执行已完成，执行记录ID：{}，状态：{}，评分：{}", executionId, STATUS_NAMES[status], accuracyScore);
        return execution;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlanExecutionDetailVO generateReplayAnalysis(Long executionId, String reviewerName) {
        PlanExecutionRecord execution = executionRecordMapper.selectById(executionId);
        if (execution == null) {
            throw new BusinessException("执行记录不存在");
        }

        List<ValveOperationRecord> valveRecords = valveOperationMapper.selectList(
                new LambdaQueryWrapper<ValveOperationRecord>()
                        .eq(ValveOperationRecord::getExecutionId, executionId)
        );

        PlanReplayAnalysis analysis = new PlanReplayAnalysis();
        analysis.setExecutionId(executionId);
        analysis.setExecutionCode(execution.getRecordCode());

        int planAccuracyScore = calculatePlanAccuracyScore(execution);
        int timeDeviationScore = calculateTimeDeviationScore(execution);
        int impactDeviationScore = calculateImpactDeviationScore(execution);
        int operationAccuracyScore = calculateOperationAccuracyScore(valveRecords);

        analysis.setPlanAccuracyScore(planAccuracyScore);
        analysis.setTimeDeviationScore(timeDeviationScore);
        analysis.setImpactDeviationScore(impactDeviationScore);
        analysis.setOperationAccuracyScore(operationAccuracyScore);

        analysis.setGoodPoints(generateGoodPoints(execution, valveRecords));
        analysis.setImprovementPoints(generateImprovementPoints(execution, valveRecords));
        analysis.setSuggestions(generateSuggestions(execution, valveRecords));
        analysis.setLearnedLessons(generateLearnedLessons(execution, valveRecords));

        analysis.setReviewerName(reviewerName);
        analysis.setReviewTime(LocalDateTime.now());

        LambdaQueryWrapper<PlanReplayAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanReplayAnalysis::getExecutionId, executionId);
        PlanReplayAnalysis existing = replayAnalysisMapper.selectOne(wrapper);
        if (existing != null) {
            analysis.setId(existing.getId());
            replayAnalysisMapper.updateById(analysis);
        } else {
            replayAnalysisMapper.insert(analysis);
        }

        return getExecutionDetail(executionId);
    }

    @Override
    public PageResult<PlanExecutionRecord> executionPage(PageQuery query, Integer status, Integer strategyType, String keyword) {
        LambdaQueryWrapper<PlanExecutionRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PlanExecutionRecord::getStatus, status);
        }
        if (strategyType != null) {
            wrapper.eq(PlanExecutionRecord::getStrategyType, strategyType);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(PlanExecutionRecord::getRecordCode, keyword)
                    .or().like(PlanExecutionRecord::getPlanName, keyword)
                    .or().like(PlanExecutionRecord::getAlarmCode, keyword));
        }
        wrapper.orderByDesc(PlanExecutionRecord::getId);

        Page<PlanExecutionRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<PlanExecutionRecord> result = executionRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    private String generateRecordCode() {
        String dateStr = LocalDateTime.now().format(DTF);
        String prefix = "PER-" + dateStr + "-";
        LambdaQueryWrapper<PlanExecutionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(PlanExecutionRecord::getRecordCode, prefix);
        wrapper.orderByDesc(PlanExecutionRecord::getRecordCode);
        wrapper.last("LIMIT 1");
        PlanExecutionRecord last = executionRecordMapper.selectOne(wrapper);

        int seq = 1;
        if (last != null && StrUtil.isNotBlank(last.getRecordCode())) {
            String[] parts = last.getRecordCode().split("-");
            if (parts.length >= 3) {
                try {
                    seq = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException e) {
                    seq = new Random().nextInt(9000) + 1000;
                }
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private PlanExecutionDetailVO.ExecutionComparisonVO buildComparison(
            PlanExecutionRecord record, List<ValveOperationRecord> valveRecords) {

        PlanExecutionDetailVO.ExecutionComparisonVO comparison = new PlanExecutionDetailVO.ExecutionComparisonVO();
        comparison.setEstimatedTotalMinutes(record.getEstimatedTotalMinutes());
        comparison.setActualTotalMinutes(record.getTotalUsedMinutes());
        if (record.getTotalUsedMinutes() != null && record.getEstimatedTotalMinutes() != null) {
            comparison.setTimeDeviation(record.getTotalUsedMinutes() - record.getEstimatedTotalMinutes());
            if (record.getEstimatedTotalMinutes() > 0) {
                double rate = (double) (record.getTotalUsedMinutes() - record.getEstimatedTotalMinutes())
                        / record.getEstimatedTotalMinutes() * 100;
                comparison.setTimeDeviationRate(Math.round(rate * 100) / 100.0);
            }
        }

        comparison.setEstimatedAffectedUsers(record.getEstimatedAffectedUsers());
        comparison.setActualAffectedUsers(record.getActualAffectedUsers());
        if (record.getActualAffectedUsers() != null && record.getEstimatedAffectedUsers() != null) {
            comparison.setUserDeviation(record.getActualAffectedUsers() - record.getEstimatedAffectedUsers());
            if (record.getEstimatedAffectedUsers() > 0) {
                double rate = (double) (record.getActualAffectedUsers() - record.getEstimatedAffectedUsers())
                        / record.getEstimatedAffectedUsers() * 100;
                comparison.setUserDeviationRate(Math.round(rate * 100) / 100.0);
            }
        }

        comparison.setEstimatedAffectedEnterprises(record.getEstimatedAffectedEnterprises());
        comparison.setActualAffectedEnterprises(record.getActualAffectedEnterprises());
        if (record.getActualAffectedEnterprises() != null && record.getEstimatedAffectedEnterprises() != null) {
            comparison.setEnterpriseDeviation(record.getActualAffectedEnterprises() - record.getEstimatedAffectedEnterprises());
        }

        int plannedCount = 0;
        int actualCount = 0;
        int successCount = 0;
        int failCount = 0;
        int errorCount = 0;

        for (ValveOperationRecord vor : valveRecords) {
            if (vor.getPlannedAction() != null && vor.getPlannedAction() != 0) {
                plannedCount++;
            }
            if (vor.getActualAction() != null && vor.getActualAction() != 0) {
                actualCount++;
            }
            if (vor.getIsSuccessful() != null && vor.getIsSuccessful() == 1) {
                successCount++;
            }
            if (vor.getIsSuccessful() != null && vor.getIsSuccessful() == 0) {
                failCount++;
            }
            if (vor.getPlannedAction() != null && vor.getActualAction() != null
                    && !vor.getPlannedAction().equals(vor.getActualAction())) {
                errorCount++;
            }
        }

        comparison.setPlannedValveCount(plannedCount);
        comparison.setActualValveCount(actualCount);
        comparison.setSuccessfulValveCount(successCount);
        comparison.setFailedValveCount(failCount);
        if (actualCount > 0) {
            double rate = (double) successCount / actualCount * 100;
            comparison.setValveSuccessRate(Math.round(rate * 100) / 100.0);
        }
        comparison.setValveErrorCount(errorCount);
        comparison.setAccuracyScore(record.getAccuracyScore());

        return comparison;
    }

    private int calculateAccuracyScore(PlanExecutionRecord execution, List<ValveOperationRecord> valveRecords) {
        int score = 100;

        if (execution.getEstimatedTotalMinutes() != null && execution.getTotalUsedMinutes() != null
                && execution.getEstimatedTotalMinutes() > 0) {
            double timeDeviationPercent = Math.abs((double) (execution.getTotalUsedMinutes() - execution.getEstimatedTotalMinutes())
                    / execution.getEstimatedTotalMinutes());
            score -= (int) (timeDeviationPercent * 30);
        }

        if (execution.getEstimatedAffectedUsers() != null && execution.getActualAffectedUsers() != null
                && execution.getEstimatedAffectedUsers() > 0) {
            double userDeviationPercent = Math.abs((double) (execution.getActualAffectedUsers() - execution.getEstimatedAffectedUsers())
                    / execution.getEstimatedAffectedUsers());
            score -= (int) (userDeviationPercent * 30);
        }

        int valveErrorCount = 0;
        for (ValveOperationRecord vor : valveRecords) {
            if (vor.getPlannedAction() != null && vor.getActualAction() != null
                    && !vor.getPlannedAction().equals(vor.getActualAction())) {
                valveErrorCount++;
            }
            if (vor.getIsSuccessful() != null && vor.getIsSuccessful() == 0) {
                valveErrorCount++;
            }
        }
        score -= valveErrorCount * 5;

        return Math.max(0, Math.min(100, score));
    }

    private int calculatePlanAccuracyScore(PlanExecutionRecord execution) {
        if (execution.getAccuracyScore() != null) {
            return execution.getAccuracyScore();
        }
        return 70;
    }

    private int calculateTimeDeviationScore(PlanExecutionRecord execution) {
        if (execution.getEstimatedTotalMinutes() == null || execution.getTotalUsedMinutes() == null
                || execution.getEstimatedTotalMinutes() == 0) {
            return 80;
        }
        double deviationPercent = Math.abs((double) (execution.getTotalUsedMinutes() - execution.getEstimatedTotalMinutes())
                / execution.getEstimatedTotalMinutes());
        int score = (int) (100 - deviationPercent * 100);
        return Math.max(0, Math.min(100, score));
    }

    private int calculateImpactDeviationScore(PlanExecutionRecord execution) {
        if (execution.getEstimatedAffectedUsers() == null || execution.getActualAffectedUsers() == null
                || execution.getEstimatedAffectedUsers() == 0) {
            return 75;
        }
        double deviationPercent = Math.abs((double) (execution.getActualAffectedUsers() - execution.getEstimatedAffectedUsers())
                / execution.getEstimatedAffectedUsers());
        int score = (int) (100 - deviationPercent * 100);
        return Math.max(0, Math.min(100, score));
    }

    private int calculateOperationAccuracyScore(List<ValveOperationRecord> valveRecords) {
        if (valveRecords == null || valveRecords.isEmpty()) {
            return 80;
        }
        int total = 0;
        int correct = 0;
        for (ValveOperationRecord vor : valveRecords) {
            if (vor.getPlannedAction() != null && vor.getPlannedAction() != 0) {
                total++;
                if (vor.getIsSuccessful() != null && vor.getIsSuccessful() == 1) {
                    correct++;
                }
            }
        }
        if (total == 0) return 80;
        return (int) ((double) correct / total * 100);
    }

    private String generateGoodPoints(PlanExecutionRecord execution, List<ValveOperationRecord> valveRecords) {
        List<String> points = new ArrayList<>();

        if (execution.getStrategyType() != null) {
            points.add("选用" + STRATEGY_NAMES[execution.getStrategyType()] + "，策略匹配合理");
        }

        if (execution.getAccuracyScore() != null && execution.getAccuracyScore() >= 80) {
            points.add("整体执行准确度高，达到" + execution.getAccuracyScore() + "分");
        }

        if (execution.getDeviationMinutes() != null && execution.getDeviationMinutes() <= 0) {
            points.add("实际执行时间比预估提前，效率较高");
        }

        long successCount = valveRecords.stream()
                .filter(v -> v.getIsSuccessful() != null && v.getIsSuccessful() == 1)
                .count();
        if (successCount > 0 && successCount == valveRecords.stream().filter(v -> v.getPlannedAction() != null && v.getPlannedAction() != 0).count()) {
            points.add("阀门操作全部成功完成");
        }

        if (points.isEmpty()) {
            points.add("按计划完成了主要处置工作");
        }

        return String.join("；", points);
    }

    private String generateImprovementPoints(PlanExecutionRecord execution, List<ValveOperationRecord> valveRecords) {
        List<String> points = new ArrayList<>();

        if (execution.getDeviationMinutes() != null && execution.getDeviationMinutes() > 0) {
            points.add("执行时间比预估慢" + execution.getDeviationMinutes() + "分钟，需优化时间估算");
        }

        if (execution.getDeviationUsers() != null && execution.getDeviationUsers() > 0) {
            points.add("实际影响用户比预估多" + execution.getDeviationUsers() + "户，影响范围评估需更精准");
        }

        long failCount = valveRecords.stream()
                .filter(v -> v.getIsSuccessful() != null && v.getIsSuccessful() == 0)
                .count();
        if (failCount > 0) {
            points.add("有" + failCount + "个阀门操作失败，需检查阀门状态和操作流程");
        }

        long errorCount = valveRecords.stream()
                .filter(v -> v.getPlannedAction() != null && v.getActualAction() != null
                        && !v.getPlannedAction().equals(v.getActualAction()))
                .count();
        if (errorCount > 0) {
            points.add("有" + errorCount + "个阀门操作与计划不符，需加强操作规范性");
        }

        if (points.isEmpty()) {
            points.add("整体执行良好，持续优化方案准确度");
        }

        return String.join("；", points);
    }

    private String generateSuggestions(PlanExecutionRecord execution, List<ValveOperationRecord> valveRecords) {
        List<String> suggestions = new ArrayList<>();

        suggestions.add("建议优化时间预估模型，结合历史数据提高预测准确度");

        if (execution.getDeviationUsers() != null && execution.getDeviationUsers() > 0) {
            suggestions.add("建议完善影响范围评估算法，结合GIS数据精算影响用户数");
        }

        long remoteCount = valveRecords.stream()
                .filter(v -> v.getRemoteControllable() != null && v.getRemoteControllable() == 1)
                .count();
        if (remoteCount < valveRecords.size() / 2) {
            suggestions.add("建议增加远程控制阀门比例，提高应急响应速度");
        }

        suggestions.add("建议定期开展应急演练，提升现场处置熟练度");

        return String.join("；", suggestions);
    }

    private String generateLearnedLessons(PlanExecutionRecord execution, List<ValveOperationRecord> valveRecords) {
        List<String> lessons = new ArrayList<>();

        lessons.add("应急处置需要快速决策与精准执行相结合");
        lessons.add("方案选择应综合考虑时间、影响、资源、安全等多维度因素");

        if (execution.getDeviationMinutes() != null && Math.abs(execution.getDeviationMinutes()) > 30) {
            lessons.add("时间预估需充分考虑现场复杂情况，预留足够缓冲时间");
        }

        long failCount = valveRecords.stream()
                .filter(v -> v.getIsSuccessful() != null && v.getIsSuccessful() == 0)
                .count();
        if (failCount > 0) {
            lessons.add("阀门设备状态直接影响处置效率，需加强日常巡检维护");
        }

        return String.join("；", lessons);
    }

    private String buildDeviationDescription(PlanExecutionRecord record, PlanExecutionDetailVO.ExecutionComparisonVO comparison) {
        StringBuilder sb = new StringBuilder();

        if (record.getDeviationMinutes() != null) {
            if (record.getDeviationMinutes() > 0) {
                sb.append("实际耗时比预估多").append(record.getDeviationMinutes()).append("分钟，进度偏慢。");
            } else if (record.getDeviationMinutes() < 0) {
                sb.append("实际耗时比预估少").append(Math.abs(record.getDeviationMinutes())).append("分钟，效率较高。");
            } else {
                sb.append("实际耗时与预估一致。");
            }
        }

        if (record.getDeviationUsers() != null) {
            if (record.getDeviationUsers() > 0) {
                sb.append("实际影响用户比预估多").append(record.getDeviationUsers()).append("户。");
            } else if (record.getDeviationUsers() < 0) {
                sb.append("实际影响用户比预估少").append(Math.abs(record.getDeviationUsers())).append("户。");
            } else {
                sb.append("影响用户数与预估一致。");
            }
        }

        if (comparison.getValveErrorCount() != null && comparison.getValveErrorCount() > 0) {
            sb.append("阀门操作有").append(comparison.getValveErrorCount()).append("处偏差。");
        }

        if (sb.length() == 0) {
            sb.append("执行情况良好，各项指标与计划基本相符。");
        }

        return sb.toString();
    }
}
