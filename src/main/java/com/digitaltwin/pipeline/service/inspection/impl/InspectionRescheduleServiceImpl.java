package com.digitaltwin.pipeline.service.inspection.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.dto.inspection.AdvancedInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.InsertionResultVO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InsertionTask;
import com.digitaltwin.pipeline.entity.inspection.InspectionTeam;
import com.digitaltwin.pipeline.entity.inspection.ScheduleChangeLog;
import com.digitaltwin.pipeline.entity.inspection.TaskAdjustment;
import com.digitaltwin.pipeline.mapper.inspection.InsertionTaskMapper;
import com.digitaltwin.pipeline.mapper.inspection.InspectionTeamMapper;
import com.digitaltwin.pipeline.mapper.inspection.ScheduleChangeLogMapper;
import com.digitaltwin.pipeline.mapper.inspection.TaskAdjustmentMapper;
import com.digitaltwin.pipeline.service.inspection.AdvancedInspectionScheduleService;
import com.digitaltwin.pipeline.service.inspection.InspectionRescheduleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class InspectionRescheduleServiceImpl implements InspectionRescheduleService {

    private final AdvancedInspectionScheduleService advancedScheduleService;
    private final ScheduleChangeLogMapper changeLogMapper;
    private final InsertionTaskMapper insertionTaskMapper;
    private final TaskAdjustmentMapper taskAdjustmentMapper;
    private final InspectionTeamMapper teamMapper;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] ADJUSTMENT_TYPE_NAMES = {"", "延期", "提前", "换人", "换车", "换班组", "取消", "优先级变更"};
    private static final String[] CHANGE_TYPE_NAMES = {"", "临时插单", "任务取消", "人员调整", "车辆调整", "任务延期", "区域变更", "优先级变更"};
    private static final String[] SOURCE_TYPE_NAMES = {"", "告警", "隐患", "工单", "开挖", "上级指令"};

    @Override
    public InsertionResultVO insertTask(InsertionTask task) {
        InsertionResultVO result = simulateInsertion(task);

        String insertionCode = generateInsertionCode();
        task.setInsertionCode(insertionCode);
        task.setStatus(1);
        if (task.getCreateTime() == null) {
            task.setCreateTime(LocalDateTime.now());
        }
        insertionTaskMapper.insert(task);

        ScheduleChangeLog changeLog = buildChangeLog(task, result);
        changeLog.setStatus(3);
        changeLogMapper.insert(changeLog);

        saveTaskAdjustments(changeLog.getId(), result);

        result.setInsertionTask(task);
        return result;
    }

    @Override
    public ScheduleChangeLog cancelTask(Long taskId, String reason, String operatorName) {
        ScheduleChangeLog changeLog = new ScheduleChangeLog();
        changeLog.setChangeCode(generateChangeCode());
        changeLog.setChangeType(2);
        changeLog.setChangeReason(6);
        changeLog.setReasonDetail(reason);
        changeLog.setScheduleDate(LocalDate.now().format(DATE_FORMATTER));
        changeLog.setOperatorName(operatorName);
        changeLog.setChangeTime(LocalDateTime.now());
        changeLog.setStatus(3);
        changeLog.setAffectedTaskCount(1);
        changeLogMapper.insert(changeLog);

        TaskAdjustment adjustment = new TaskAdjustment();
        adjustment.setChangeLogId(changeLog.getId());
        adjustment.setOriginalTaskId(taskId);
        adjustment.setOriginalTaskCode("TASK-" + taskId);
        adjustment.setAdjustmentType(6);
        adjustment.setBeforeValue("正常");
        adjustment.setAfterValue("已取消");
        adjustment.setReason(reason);
        adjustment.setOperatorName(operatorName);
        adjustment.setOperationTime(LocalDateTime.now());
        adjustment.setIsNotified(0);
        taskAdjustmentMapper.insert(adjustment);

        return changeLog;
    }

    @Override
    public ScheduleChangeLog adjustTaskPerson(Long taskId, Long newPersonId, String reason, String operatorName) {
        ScheduleChangeLog changeLog = new ScheduleChangeLog();
        changeLog.setChangeCode(generateChangeCode());
        changeLog.setChangeType(3);
        changeLog.setChangeReason(6);
        changeLog.setReasonDetail(reason);
        changeLog.setScheduleDate(LocalDate.now().format(DATE_FORMATTER));
        changeLog.setOperatorName(operatorName);
        changeLog.setChangeTime(LocalDateTime.now());
        changeLog.setStatus(3);
        changeLog.setAffectedTaskCount(1);
        changeLogMapper.insert(changeLog);

        TaskAdjustment adjustment = new TaskAdjustment();
        adjustment.setChangeLogId(changeLog.getId());
        adjustment.setOriginalTaskId(taskId);
        adjustment.setOriginalTaskCode("TASK-" + taskId);
        adjustment.setAdjustmentType(3);
        adjustment.setBeforeValue("原人员");
        adjustment.setAfterValue("人员ID:" + newPersonId);
        adjustment.setReason(reason);
        adjustment.setOperatorName(operatorName);
        adjustment.setOperationTime(LocalDateTime.now());
        adjustment.setIsNotified(0);
        taskAdjustmentMapper.insert(adjustment);

        return changeLog;
    }

    @Override
    public ScheduleChangeLog adjustTaskVehicle(Long taskId, Long newVehicleId, String reason, String operatorName) {
        ScheduleChangeLog changeLog = new ScheduleChangeLog();
        changeLog.setChangeCode(generateChangeCode());
        changeLog.setChangeType(4);
        changeLog.setChangeReason(6);
        changeLog.setReasonDetail(reason);
        changeLog.setScheduleDate(LocalDate.now().format(DATE_FORMATTER));
        changeLog.setOperatorName(operatorName);
        changeLog.setChangeTime(LocalDateTime.now());
        changeLog.setStatus(3);
        changeLog.setAffectedTaskCount(1);
        changeLogMapper.insert(changeLog);

        TaskAdjustment adjustment = new TaskAdjustment();
        adjustment.setChangeLogId(changeLog.getId());
        adjustment.setOriginalTaskId(taskId);
        adjustment.setOriginalTaskCode("TASK-" + taskId);
        adjustment.setAdjustmentType(4);
        adjustment.setBeforeValue("原车辆");
        adjustment.setAfterValue("车辆ID:" + newVehicleId);
        adjustment.setReason(reason);
        adjustment.setOperatorName(operatorName);
        adjustment.setOperationTime(LocalDateTime.now());
        adjustment.setIsNotified(0);
        taskAdjustmentMapper.insert(adjustment);

        return changeLog;
    }

    @Override
    public List<ScheduleChangeLog> getDayChangeLogs(String date) {
        String scheduleDate = StrUtil.isNotBlank(date) ? date : LocalDate.now().format(DATE_FORMATTER);
        return changeLogMapper.selectList(new LambdaQueryWrapper<ScheduleChangeLog>()
                .eq(ScheduleChangeLog::getScheduleDate, scheduleDate)
                .orderByDesc(ScheduleChangeLog::getChangeTime));
    }

    @Override
    public InsertionResultVO simulateInsertion(InsertionTask task) {
        InsertionResultVO result = new InsertionResultVO();
        result.setInsertionTask(task);

        AdvancedInspectionScheduleDTO currentSchedule = loadCurrentSchedule();
        String beforeSnapshot = toJson(currentSchedule);

        List<InspectionTeam> teams = loadTeams();
        if (CollUtil.isEmpty(teams)) {
            result.setImpactAssessment("无可用班组");
            return result;
        }

        InspectionTeam bestTeam = findBestTeam(task, teams, currentSchedule);
        if (bestTeam == null) {
            result.setImpactAssessment("未找到合适的班组");
            result.setNeedReinforcement(true);
            result.setSuggestionPlans(buildSuggestionPlans(task, teams, null));
            return result;
        }

        AdvancedInspectionScheduleDTO.TeamDailySchedule teamSchedule = findTeamSchedule(bestTeam.getId(), currentSchedule);
        if (teamSchedule == null) {
            teamSchedule = buildEmptyTeamSchedule(bestTeam);
        }

        int insertIndex = findBestInsertIndex(task, teamSchedule);
        InsertionResultVO.AffectedTeamVO affectedTeam = buildAffectedTeam(task, bestTeam, teamSchedule, insertIndex);

        result.setAffectedTeams(Collections.singletonList(affectedTeam));

        BigDecimal hoursChange = BigDecimal.valueOf(task.getEstimatedMinutes() != null ? task.getEstimatedMinutes() / 60.0 : 1.0)
                .setScale(2, RoundingMode.HALF_UP);
        boolean needOvertime = checkNeedOvertime(bestTeam, teamSchedule, hoursChange);
        result.setNeedOvertime(needOvertime);
        result.setNeedReinforcement(needOvertime && teams.size() <= 1);

        if (teamSchedule.getTimeSlots() != null && insertIndex < teamSchedule.getTimeSlots().size()) {
            AdvancedInspectionScheduleDTO.TimeSlotTask slot = teamSchedule.getTimeSlots().get(Math.max(0, insertIndex - 1));
            result.setInsertStartTime(slot != null ? slot.getEndTime() : bestTeam.getWorkStartTime());
            LocalTime startTime = LocalTime.parse(result.getInsertStartTime(), TIME_FORMATTER);
            int duration = task.getEstimatedMinutes() != null ? task.getEstimatedMinutes() : 60;
            result.setInsertEndTime(startTime.plusMinutes(duration).format(TIME_FORMATTER));
        } else {
            result.setInsertStartTime(bestTeam.getWorkEndTime());
            result.setInsertEndTime("18:30");
        }

        result.setImpactAssessment(buildImpactAssessment(affectedTeam, needOvertime));
        result.setSuggestionPlans(buildSuggestionPlans(task, teams, bestTeam));

        return result;
    }

    private AdvancedInspectionScheduleDTO loadCurrentSchedule() {
        try {
            return advancedScheduleService.advancedSchedule(new SmartInspectionScheduleQueryDTO());
        } catch (Exception e) {
            return buildMockSchedule();
        }
    }

    private AdvancedInspectionScheduleDTO buildMockSchedule() {
        AdvancedInspectionScheduleDTO dto = new AdvancedInspectionScheduleDTO();
        dto.setScheduleDate(LocalDate.now().format(DATE_FORMATTER));
        List<AdvancedInspectionScheduleDTO.TeamDailySchedule> teamSchedules = new ArrayList<>();

        List<InspectionTeam> teams = loadTeams();
        int taskIdx = 0;
        for (InspectionTeam team : teams) {
            AdvancedInspectionScheduleDTO.TeamDailySchedule schedule = new AdvancedInspectionScheduleDTO.TeamDailySchedule();
            schedule.setTeamId(team.getId());
            schedule.setTeamCode(team.getTeamCode());
            schedule.setTeamName(team.getTeamName());
            schedule.setTotalHours(BigDecimal.valueOf(6));
            schedule.setTaskCount(5);
            schedule.setTimeSlots(buildMockTimeSlots(team, taskIdx++));
            teamSchedules.add(schedule);
        }
        dto.setTeamSchedules(teamSchedules);
        return dto;
    }

    private List<AdvancedInspectionScheduleDTO.TimeSlotTask> buildMockTimeSlots(InspectionTeam team, int offset) {
        List<AdvancedInspectionScheduleDTO.TimeSlotTask> slots = new ArrayList<>();
        LocalTime start = LocalTime.parse(team.getWorkStartTime() != null ? team.getWorkStartTime() : "08:30", TIME_FORMATTER);
        String[] locations = {"东门巡检点", "南门巡检点", "西门巡检点", "北门巡检点", "中心巡检点"};
        for (int i = 0; i < 5; i++) {
            AdvancedInspectionScheduleDTO.TimeSlotTask slot = new AdvancedInspectionScheduleDTO.TimeSlotTask();
            slot.setSlotIndex(i + offset);
            slot.setStartTime(start.format(TIME_FORMATTER));
            slot.setEndTime(start.plusMinutes(60).format(TIME_FORMATTER));
            slot.setTaskType(1);
            slot.setTaskTypeName("巡检");
            slot.setTaskTitle(locations[i % locations.length] + "巡检");
            slot.setLocationName(locations[i % locations.length]);
            slot.setDurationMinutes(60);
            slot.setPriority(2);
            slots.add(slot);
            start = start.plusMinutes(90);
        }
        return slots;
    }

    private List<InspectionTeam> loadTeams() {
        List<InspectionTeam> teams = teamMapper.selectList(new LambdaQueryWrapper<InspectionTeam>()
                .eq(InspectionTeam::getStatus, 1));
        if (CollUtil.isNotEmpty(teams)) {
            return teams;
        }
        return buildMockTeams();
    }

    private List<InspectionTeam> buildMockTeams() {
        List<InspectionTeam> list = new ArrayList<>();
        String[][] data = {
                {"TEAM001", "城东巡检一班", "张伟", "13800000001", "AREA_EAST", "城东区", "3", "GAS,WATER"},
                {"TEAM002", "城西巡检二班", "李强", "13800000002", "AREA_WEST", "城西区", "3", "POWER,WATER"},
                {"TEAM003", "城南巡检三班", "王芳", "13800000003", "AREA_SOUTH", "城南区", "2", "GAS,POWER"}
        };
        long id = 1;
        for (String[] d : data) {
            InspectionTeam t = new InspectionTeam();
            t.setId(id);
            t.setTeamCode(d[0]);
            t.setTeamName(d[1]);
            t.setLeaderName(d[2]);
            t.setContactPhone(d[3]);
            t.setAreaCode(d[4]);
            t.setAreaName(d[5]);
            t.setMemberCount(Integer.parseInt(d[6]));
            t.setMaxDailyHours(BigDecimal.valueOf(8));
            t.setMaxDailyKm(BigDecimal.valueOf(15));
            t.setWorkStartTime("08:30");
            t.setWorkEndTime("17:30");
            t.setCapabilities(d[7]);
            t.setStatus(1);
            list.add(t);
            id++;
        }
        return list;
    }

    private InspectionTeam findBestTeam(InsertionTask task, List<InspectionTeam> teams, AdvancedInspectionScheduleDTO schedule) {
        List<TeamScore> scores = new ArrayList<>();
        for (InspectionTeam team : teams) {
            double score = 0;

            if (StrUtil.isNotBlank(task.getRequiredSkills()) && StrUtil.isNotBlank(team.getCapabilities())) {
                Set<String> required = new HashSet<>(Arrays.asList(task.getRequiredSkills().split(",")));
                Set<String> capabilities = new HashSet<>(Arrays.asList(team.getCapabilities().split(",")));
                long matchCount = required.stream().filter(capabilities::contains).count();
                score += matchCount * 20;
            } else if (StrUtil.isBlank(task.getRequiredSkills())) {
                score += 20;
            }

            if (StrUtil.isNotBlank(task.getAreaCode()) && StrUtil.isNotBlank(team.getAreaCode())) {
                if (task.getAreaCode().equals(team.getAreaCode())) {
                    score += 30;
                }
            }

            AdvancedInspectionScheduleDTO.TeamDailySchedule teamSchedule = findTeamSchedule(team.getId(), schedule);
            if (teamSchedule != null && teamSchedule.getTotalHours() != null) {
                BigDecimal maxHours = team.getMaxDailyHours() != null ? team.getMaxDailyHours() : BigDecimal.valueOf(8);
                BigDecimal remaining = maxHours.subtract(teamSchedule.getTotalHours());
                score += remaining.doubleValue() * 2;
            } else {
                score += 16;
            }

            scores.add(new TeamScore(team, score));
        }

        scores.sort((a, b) -> Double.compare(b.score, a.score));
        return scores.isEmpty() ? null : scores.get(0).team;
    }

    private AdvancedInspectionScheduleDTO.TeamDailySchedule findTeamSchedule(Long teamId, AdvancedInspectionScheduleDTO schedule) {
        if (schedule == null || CollUtil.isEmpty(schedule.getTeamSchedules())) {
            return null;
        }
        return schedule.getTeamSchedules().stream()
                .filter(s -> teamId.equals(s.getTeamId()))
                .findFirst()
                .orElse(null);
    }

    private AdvancedInspectionScheduleDTO.TeamDailySchedule buildEmptyTeamSchedule(InspectionTeam team) {
        AdvancedInspectionScheduleDTO.TeamDailySchedule schedule = new AdvancedInspectionScheduleDTO.TeamDailySchedule();
        schedule.setTeamId(team.getId());
        schedule.setTeamCode(team.getTeamCode());
        schedule.setTeamName(team.getTeamName());
        schedule.setTotalHours(BigDecimal.ZERO);
        schedule.setTaskCount(0);
        schedule.setTimeSlots(new ArrayList<>());
        return schedule;
    }

    private int findBestInsertIndex(InsertionTask task, AdvancedInspectionScheduleDTO.TeamDailySchedule teamSchedule) {
        List<AdvancedInspectionScheduleDTO.TimeSlotTask> slots = teamSchedule.getTimeSlots();
        if (CollUtil.isEmpty(slots)) {
            return 0;
        }

        int priority = task.getPriority() != null ? task.getPriority() : 2;

        for (int i = 0; i < slots.size(); i++) {
            AdvancedInspectionScheduleDTO.TimeSlotTask slot = slots.get(i);
            if (slot.getPriority() != null && priority > slot.getPriority()) {
                return i;
            }
        }
        return slots.size();
    }

    private InsertionResultVO.AffectedTeamVO buildAffectedTeam(InsertionTask task, InspectionTeam team,
                                                            AdvancedInspectionScheduleDTO.TeamDailySchedule teamSchedule, int insertIndex) {
        InsertionResultVO.AffectedTeamVO affected = new InsertionResultVO.AffectedTeamVO();
        affected.setTeamId(team.getId());
        affected.setTeamName(team.getTeamName());
        affected.setOriginalTaskCount(teamSchedule.getTaskCount() != null ? teamSchedule.getTaskCount() : 0);
        affected.setNewTaskCount(affected.getOriginalTaskCount() + 1);

        BigDecimal hoursChange = BigDecimal.valueOf(task.getEstimatedMinutes() != null ? task.getEstimatedMinutes() / 60.0 : 1.0)
                .setScale(2, RoundingMode.HALF_UP);
        affected.setTotalHoursChange(hoursChange);

        List<InsertionResultVO.AdjustedTaskVO> adjustedTasks = new ArrayList<>();
        List<AdvancedInspectionScheduleDTO.TimeSlotTask> slots = teamSchedule.getTimeSlots();
        if (CollUtil.isNotEmpty(slots) && insertIndex < slots.size()) {
            int delayMinutes = task.getEstimatedMinutes() != null ? task.getEstimatedMinutes() : 60;
            for (int i = insertIndex; i < slots.size(); i++) {
                AdvancedInspectionScheduleDTO.TimeSlotTask slot = slots.get(i);
                InsertionResultVO.AdjustedTaskVO adjusted = new InsertionResultVO.AdjustedTaskVO();
                adjusted.setTaskId((long) (i + 1));
                adjusted.setTaskCode("TASK-" + (i + 1));
                adjusted.setTaskTitle(slot.getTaskTitle());
                adjusted.setOriginalTime(slot.getStartTime() + "-" + slot.getEndTime());

                LocalTime origStart = LocalTime.parse(slot.getStartTime(), TIME_FORMATTER);
                LocalTime origEnd = LocalTime.parse(slot.getEndTime(), TIME_FORMATTER);
                adjusted.setNewTime(origStart.plusMinutes(delayMinutes).format(TIME_FORMATTER)
                        + "-" + origEnd.plusMinutes(delayMinutes).format(TIME_FORMATTER));
                adjusted.setAdjustmentType(1);
                adjusted.setAdjustmentTypeName(ADJUSTMENT_TYPE_NAMES[1]);
                adjusted.setAdjustmentReason("临时插单顺延");
                adjustedTasks.add(adjusted);
            }
        }
        affected.setAdjustedTasks(adjustedTasks);

        return affected;
    }

    private boolean checkNeedOvertime(InspectionTeam team, AdvancedInspectionScheduleDTO.TeamDailySchedule teamSchedule, BigDecimal hoursChange) {
        BigDecimal currentHours = teamSchedule.getTotalHours() != null ? teamSchedule.getTotalHours() : BigDecimal.ZERO;
        BigDecimal maxHours = team.getMaxDailyHours() != null ? team.getMaxDailyHours() : BigDecimal.valueOf(8);
        return currentHours.add(hoursChange).compareTo(maxHours) > 0;
    }

    private String buildImpactAssessment(InsertionResultVO.AffectedTeamVO affectedTeam, boolean needOvertime) {
        StringBuilder sb = new StringBuilder();
        sb.append("受影响班组：").append(affectedTeam.getTeamName());
        sb.append("，新增任务1个，顺延任务").append(affectedTeam.getAdjustedTasks().size()).append("个");
        sb.append("，工时增加").append(affectedTeam.getTotalHoursChange()).append("小时");
        if (needOvertime) {
            sb.append("，存在加班风险");
        }
        return sb.toString();
    }

    private List<InsertionResultVO.SuggestionPlanVO> buildSuggestionPlans(InsertionTask task, List<InspectionTeam> teams, InspectionTeam selectedTeam) {
        List<InsertionResultVO.SuggestionPlanVO> plans = new ArrayList<>();

        InsertionResultVO.SuggestionPlanVO planA = new InsertionResultVO.SuggestionPlanVO();
        planA.setPlanCode("A");
        planA.setPlanName("最近班组插入");
        planA.setDescription("将任务插入到距离最近、能力匹配的班组日程中，后续任务顺延");
        planA.setEstimatedHoursChange(BigDecimal.valueOf(1.0));
        planA.setRecommended(selectedTeam != null);
        plans.add(planA);

        if (teams.size() > 1) {
            InsertionResultVO.SuggestionPlanVO planB = new InsertionResultVO.SuggestionPlanVO();
            planB.setPlanCode("B");
            planB.setPlanName("调派空闲班组");
            planB.setDescription("调派工时余量最大的空闲班组执行此任务，不影响其他班组");
            planB.setEstimatedHoursChange(BigDecimal.valueOf(0.5));
            planB.setRecommended(false);
            plans.add(planB);
        }

        InsertionResultVO.SuggestionPlanVO planC = new InsertionResultVO.SuggestionPlanVO();
        planC.setPlanCode("C");
        planC.setPlanName("加班完成");
        planC.setDescription("由当前班组加班完成此任务，可能需要支付加班费用");
        planC.setEstimatedHoursChange(BigDecimal.valueOf(1.5));
        planC.setRecommended(false);
        plans.add(planC);

        return plans;
    }

    private ScheduleChangeLog buildChangeLog(InsertionTask task, InsertionResultVO result) {
        ScheduleChangeLog log = new ScheduleChangeLog();
        log.setChangeCode(generateChangeCode());
        log.setChangeType(1);
        log.setChangeReason(task.getSourceType() != null ? mapSourceToReason(task.getSourceType()) : 6);
        log.setReasonDetail(task.getInsertReason());
        log.setScheduleDate(LocalDate.now().format(DATE_FORMATTER));

        if (CollUtil.isNotEmpty(result.getAffectedTeams())) {
            String teamIds = result.getAffectedTeams().stream()
                    .map(t -> String.valueOf(t.getTeamId()))
                    .collect(Collectors.joining(","));
            String teamNames = result.getAffectedTeams().stream()
                    .map(InsertionResultVO.AffectedTeamVO::getTeamName)
                    .collect(Collectors.joining(","));
            log.setAffectedTeamIds(teamIds);
            log.setAffectedTeamNames(teamNames);

            int totalAffected = result.getAffectedTeams().stream()
                    .mapToInt(t -> t.getAdjustedTasks() != null ? t.getAdjustedTasks().size() : 0)
                    .sum() + 1;
            log.setAffectedTaskCount(totalAffected);
        }

        log.setOperatorName(task.getOperatorName());
        log.setChangeTime(LocalDateTime.now());
        log.setBeforeSnapshot("{}");
        log.setAfterSnapshot("{}");
        log.setStatus(2);
        log.setRemark("临时插单：" + task.getTitle());

        return log;
    }

    private int mapSourceToReason(int sourceType) {
        switch (sourceType) {
            case 1: return 1;
            case 2: return 2;
            case 5: return 5;
            default: return 6;
        }
    }

    private void saveTaskAdjustments(Long changeLogId, InsertionResultVO result) {
        if (CollUtil.isEmpty(result.getAffectedTeams())) {
            return;
        }
        for (InsertionResultVO.AffectedTeamVO team : result.getAffectedTeams()) {
            if (CollUtil.isEmpty(team.getAdjustedTasks())) {
                continue;
            }
            for (InsertionResultVO.AdjustedTaskVO adjusted : team.getAdjustedTasks()) {
                TaskAdjustment adj = new TaskAdjustment();
                adj.setChangeLogId(changeLogId);
                adj.setOriginalTaskId(adjusted.getTaskId());
                adj.setOriginalTaskCode(adjusted.getTaskCode());
                adj.setAdjustmentType(adjusted.getAdjustmentType());
                adj.setBeforeValue(adjusted.getOriginalTime());
                adj.setAfterValue(adjusted.getNewTime());
                adj.setReason(adjusted.getAdjustmentReason());
                adj.setOperatorName(result.getInsertionTask() != null ? result.getInsertionTask().getOperatorName() : "");
                adj.setOperationTime(LocalDateTime.now());
                adj.setIsNotified(0);
                taskAdjustmentMapper.insert(adj);
            }
        }
    }

    private String generateChangeCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = new Random().nextInt(10000);
        return "SCH-" + date + "-" + String.format("%04d", random);
    }

    private String generateInsertionCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = new Random().nextInt(10000);
        return "INS-" + date + "-" + String.format("%04d", random);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private static class TeamScore {
        InspectionTeam team;
        double score;

        TeamScore(InspectionTeam team, double score) {
            this.team = team;
            this.score = score;
        }
    }
}
