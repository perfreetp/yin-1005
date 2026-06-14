package com.digitaltwin.pipeline.service.inspection.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.dto.inspection.AdvancedInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InspectionTeam;
import com.digitaltwin.pipeline.entity.inspection.InspectionVehicle;
import com.digitaltwin.pipeline.entity.inspection.Inspector;
import com.digitaltwin.pipeline.mapper.inspection.InspectionTeamMapper;
import com.digitaltwin.pipeline.mapper.inspection.InspectionVehicleMapper;
import com.digitaltwin.pipeline.mapper.inspection.InspectorMapper;
import com.digitaltwin.pipeline.service.inspection.AdvancedInspectionScheduleService;
import com.digitaltwin.pipeline.service.inspection.SmartInspectionScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvancedInspectionScheduleServiceImpl implements AdvancedInspectionScheduleService {

    private final SmartInspectionScheduleService smartScheduleService;
    private final InspectionTeamMapper teamMapper;
    private final InspectorMapper inspectorMapper;
    private final InspectionVehicleMapper vehicleMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] TASK_TYPE_NAMES = {"", "巡检", "行驶", "休息", "工单处置"};
    private static final String[] VEHICLE_TYPE_NAMES = {"", "巡检车", "抢修车", "工程车"};
    private static final String[] CONFLICT_TYPE_NAMES = {"", "能力不匹配", "超工时", "车辆不足", "跨区域", "技能不足"};
    private static final String[] SEVERITY_NAMES = {"", "提示", "警告", "严重"};

    @Override
    public AdvancedInspectionScheduleDTO advancedSchedule(SmartInspectionScheduleQueryDTO query) {
        SmartInspectionScheduleDTO base = smartScheduleService.generateSchedule(query);

        AdvancedInspectionScheduleDTO result = new AdvancedInspectionScheduleDTO();
        copyBaseFields(base, result);

        List<InspectionTeam> teams = loadTeams();
        List<Inspector> inspectors = loadInspectors();
        List<InspectionVehicle> vehicles = loadVehicles();

        AdvancedInspectionScheduleDTO.ResourceUsageVO usage = buildResourceUsage(teams, inspectors, vehicles);
        result.setResourceUsage(usage);

        List<AdvancedInspectionScheduleDTO.SchedulingConflictVO> conflicts = new ArrayList<>();

        List<AdvancedInspectionScheduleDTO.TeamDailySchedule> teamSchedules = new ArrayList<>();
        List<SmartInspectionScheduleDTO.TeamTaskGroup> taskGroups = base.getTeamTaskGroups();

        Map<Long, List<Inspector>> inspectorByTeam = inspectors.stream()
                .collect(Collectors.groupingBy(i -> i.getTeamId() != null ? i.getTeamId() : -1L));
        Map<Long, List<InspectionVehicle>> vehicleByTeam = vehicles.stream()
                .collect(Collectors.groupingBy(v -> v.getTeamId() != null ? v.getTeamId() : -1L));

        int teamIdx = 0;
        for (SmartInspectionScheduleDTO.TeamTaskGroup group : taskGroups) {
            if (teamIdx < teams.size()) {
                InspectionTeam team = teams.get(teamIdx);
                List<Inspector> teamMembers = inspectorByTeam.getOrDefault(team.getId(), new ArrayList<>());
                InspectionVehicle teamVehicle = CollUtil.isNotEmpty(vehicleByTeam.get(team.getId()))
                        ? vehicleByTeam.get(team.getId()).get(0) : null;

                matchTeamCapability(team, group, conflicts);
                checkOvertime(team, group, conflicts);

                AdvancedInspectionScheduleDTO.TeamDailySchedule schedule = buildTeamSchedule(
                        team, teamMembers, teamVehicle, group, conflicts);
                teamSchedules.add(schedule);
            }
            teamIdx++;
        }

        if (taskGroups.size() > teams.size()) {
            AdvancedInspectionScheduleDTO.SchedulingConflictVO conflict = new AdvancedInspectionScheduleDTO.SchedulingConflictVO();
            conflict.setConflictType(3);
            conflict.setConflictTypeName(CONFLICT_TYPE_NAMES[3]);
            conflict.setSeverity(2);
            conflict.setSeverityName(SEVERITY_NAMES[2]);
            conflict.setDescription("任务组数(" + taskGroups.size() + ")超过可用班组数(" + teams.size() + ")，车辆资源不足");
            conflict.setSuggestion("增加班组或调整任务优先级，延后低优先级任务");
            conflicts.add(conflict);
        }

        result.setTeamSchedules(teamSchedules);
        result.setConflicts(conflicts);
        result.setGantt(buildGantt(teamSchedules));

        updateOvertimeWarnings(result, teamSchedules);

        return result;
    }

    private void copyBaseFields(SmartInspectionScheduleDTO from, AdvancedInspectionScheduleDTO to) {
        to.setScheduleDate(from.getScheduleDate());
        to.setGenerateTime(from.getGenerateTime());
        to.setTotalPoints(from.getTotalPoints());
        to.setTotalDistanceKm(from.getTotalDistanceKm());
        to.setEstimatedTotalHours(from.getEstimatedTotalHours());
        to.setSuggestedTeamCount(from.getSuggestedTeamCount());
        to.setAreaStatistics(from.getAreaStatistics());
        to.setRiskDistribution(from.getRiskDistribution());
        to.setTeamTaskGroups(from.getTeamTaskGroups());
        to.setPostponedPoints(from.getPostponedPoints());
    }

    private List<InspectionTeam> loadTeams() {
        List<InspectionTeam> teams = teamMapper.selectList(new LambdaQueryWrapper<InspectionTeam>()
                .eq(InspectionTeam::getStatus, 1));
        if (CollUtil.isNotEmpty(teams)) {
            return teams;
        }
        return buildMockTeams();
    }

    private List<Inspector> loadInspectors() {
        List<Inspector> list = inspectorMapper.selectList(new LambdaQueryWrapper<Inspector>());
        if (CollUtil.isNotEmpty(list)) {
            return list;
        }
        return buildMockInspectors();
    }

    private List<InspectionVehicle> loadVehicles() {
        List<InspectionVehicle> list = vehicleMapper.selectList(new LambdaQueryWrapper<InspectionVehicle>()
                .eq(InspectionVehicle::getStatus, 1));
        if (CollUtil.isNotEmpty(list)) {
            return list;
        }
        return buildMockVehicles();
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

    private List<Inspector> buildMockInspectors() {
        List<Inspector> list = new ArrayList<>();
        Object[][] data = {
                {1L, "INS001", "陈刚", "13800000101", 1L, "城东巡检一班", "燃气巡检,水管巡检", 5, 1, 116.407, 39.904, 0},
                {2L, "INS002", "刘洋", "13800000102", 1L, "城东巡检一班", "水管巡检,阀门操作", 3, 1, 116.408, 39.905, 0},
                {3L, "INS003", "赵磊", "13800000103", 1L, "城东巡检一班", "燃气巡检,应急处置", 4, 1, 116.406, 39.903, 0},
                {4L, "INS004", "孙明", "13800000104", 2L, "城西巡检二班", "电力巡检,水管巡检", 6, 1, 116.397, 39.910, 0},
                {5L, "INS005", "周杰", "13800000105", 2L, "城西巡检二班", "电力巡检,电缆检测", 2, 1, 116.398, 39.911, 0},
                {6L, "INS006", "吴浩", "13800000106", 2L, "城西巡检二班", "水管巡检,抢修", 7, 1, 116.396, 39.909, 0},
                {7L, "INS007", "郑涛", "13800000107", 3L, "城南巡检三班", "燃气巡检,电力巡检", 4, 1, 116.412, 39.898, 0},
                {8L, "INS008", "冯军", "13800000108", 3L, "城南巡检三班", "燃气巡检,带气作业", 5, 1, 116.413, 39.899, 0}
        };
        for (Object[] d : data) {
            Inspector ins = new Inspector();
            ins.setId((Long) d[0]);
            ins.setInspectorCode((String) d[1]);
            ins.setName((String) d[2]);
            ins.setPhone((String) d[3]);
            ins.setTeamId((Long) d[4]);
            ins.setTeamName((String) d[5]);
            ins.setSkills((String) d[6]);
            ins.setExperienceYears((Integer) d[7]);
            ins.setWorkStatus((Integer) d[8]);
            ins.setCurrentLng(BigDecimal.valueOf((Double) d[9]));
            ins.setCurrentLat(BigDecimal.valueOf((Double) d[10]));
            ins.setDailyWorkedHours(BigDecimal.valueOf((Integer) d[11]));
            list.add(ins);
        }
        return list;
    }

    private List<InspectionVehicle> buildMockVehicles() {
        List<InspectionVehicle> list = new ArrayList<>();
        Object[][] data = {
                {1L, "京A12345", 1, 1L, 500, 116.405, 39.902},
                {2L, "京B67890", 1, 2L, 500, 116.395, 39.908},
                {3L, "京C11111", 2, 3L, 800, 116.410, 39.897}
        };
        for (Object[] d : data) {
            InspectionVehicle v = new InspectionVehicle();
            v.setId((Long) d[0]);
            v.setPlateNumber((String) d[1]);
            v.setVehicleType((Integer) d[2]);
            v.setTeamId((Long) d[3]);
            v.setStatus(1);
            v.setLoadCapacity((Integer) d[4]);
            v.setCurrentLng(BigDecimal.valueOf((Double) d[5]));
            v.setCurrentLat(BigDecimal.valueOf((Double) d[6]));
            list.add(v);
        }
        return list;
    }

    private AdvancedInspectionScheduleDTO.TeamDailySchedule buildTeamSchedule(
            InspectionTeam team, List<Inspector> members, InspectionVehicle vehicle,
            SmartInspectionScheduleDTO.TeamTaskGroup group,
            List<AdvancedInspectionScheduleDTO.SchedulingConflictVO> conflicts) {

        AdvancedInspectionScheduleDTO.TeamDailySchedule s = new AdvancedInspectionScheduleDTO.TeamDailySchedule();
        s.setTeamId(team.getId());
        s.setTeamCode(team.getTeamCode());
        s.setTeamName(team.getTeamName());
        s.setLeaderName(team.getLeaderName());
        s.setContactPhone(team.getContactPhone());
        s.setMembers(toInspectorVOs(members));
        s.setAssignedVehicle(toVehicleVO(vehicle));
        s.setTotalHours(group.getEstimatedHours() != null ? group.getEstimatedHours() : BigDecimal.ZERO);
        s.setTotalKm(group.getTotalDistanceKm() != null ? group.getTotalDistanceKm() : BigDecimal.ZERO);
        s.setTaskCount(group.getTaskCount() != null ? group.getTaskCount() : 0);
        s.setTimeSlots(buildTimeSlots(team, group));
        return s;
    }

    private List<AdvancedInspectionScheduleDTO.InspectorVO> toInspectorVOs(List<Inspector> inspectors) {
        List<AdvancedInspectionScheduleDTO.InspectorVO> result = new ArrayList<>();
        if (CollUtil.isEmpty(inspectors)) return result;
        for (Inspector ins : inspectors) {
            AdvancedInspectionScheduleDTO.InspectorVO vo = new AdvancedInspectionScheduleDTO.InspectorVO();
            vo.setId(ins.getId());
            vo.setInspectorCode(ins.getInspectorCode());
            vo.setName(ins.getName());
            vo.setPhone(ins.getPhone());
            vo.setSkills(ins.getSkills());
            vo.setExperienceYears(ins.getExperienceYears());
            vo.setWorkStatus(ins.getWorkStatus());
            result.add(vo);
        }
        return result;
    }

    private AdvancedInspectionScheduleDTO.VehicleVO toVehicleVO(InspectionVehicle v) {
        if (v == null) return null;
        AdvancedInspectionScheduleDTO.VehicleVO vo = new AdvancedInspectionScheduleDTO.VehicleVO();
        vo.setId(v.getId());
        vo.setPlateNumber(v.getPlateNumber());
        vo.setVehicleType(v.getVehicleType());
        vo.setVehicleTypeName(v.getVehicleType() != null ? VEHICLE_TYPE_NAMES[v.getVehicleType()] : "");
        vo.setLoadCapacity(v.getLoadCapacity());
        return vo;
    }

    private List<AdvancedInspectionScheduleDTO.TimeSlotTask> buildTimeSlots(
            InspectionTeam team, SmartInspectionScheduleDTO.TeamTaskGroup group) {

        List<AdvancedInspectionScheduleDTO.TimeSlotTask> slots = new ArrayList<>();
        LocalTime workStart = parseTimeSafe(team.getWorkStartTime(), LocalTime.of(8, 30));
        LocalTime cursor = workStart;
        int slotIndex = 0;

        List<SmartInspectionScheduleDTO.ScheduledInspectionTask> tasks = group.getOrderedTasks();
        if (CollUtil.isEmpty(tasks)) return slots;

        for (SmartInspectionScheduleDTO.ScheduledInspectionTask task : tasks) {
            if (task.getDistanceFromPrev() != null && task.getDistanceFromPrev().compareTo(BigDecimal.ZERO) > 0) {
                double distKm = task.getDistanceFromPrev().doubleValue() / 1000.0;
                int driveMin = Math.max(30, (int) Math.ceil(distKm / 30.0 * 60));
                driveMin = alignTo30(driveMin);
                int driveSlots = driveMin / 30;
                for (int i = 0; i < driveSlots; i++) {
                    slots.add(buildSlot(slotIndex++, cursor, 2, "行驶中", "前往下一巡检点",
                            task.getLng(), task.getLat(), 30, task.getPriorityScore()));
                    cursor = cursor.plusMinutes(30);
                }
            }

            int workMin = alignTo30(task.getAssetType() != null && task.getAssetType() == 5 ? 60 : 30);
            int taskType = (task.getAssetType() != null && task.getAssetType() == 5) ? 4 : 1;
            String title = taskType == 4 ? "工单处置" : "巡检作业";
            int workSlots = workMin / 30;
            for (int i = 0; i < workSlots; i++) {
                slots.add(buildSlot(slotIndex++, cursor, taskType, title, task.getLocationName(),
                        task.getLng(), task.getLat(), 30, task.getPriorityScore()));
                cursor = cursor.plusMinutes(30);
            }
        }

        return slots;
    }

    private AdvancedInspectionScheduleDTO.TimeSlotTask buildSlot(
            int slotIndex, LocalTime start, int taskType, String title,
            String locationName, BigDecimal lng, BigDecimal lat, int duration, Integer priority) {
        AdvancedInspectionScheduleDTO.TimeSlotTask slot = new AdvancedInspectionScheduleDTO.TimeSlotTask();
        slot.setSlotIndex(slotIndex);
        slot.setStartTime(start.format(TIME_FORMATTER));
        slot.setEndTime(start.plusMinutes(duration).format(TIME_FORMATTER));
        slot.setTaskType(taskType);
        slot.setTaskTypeName(TASK_TYPE_NAMES[taskType]);
        slot.setTaskTitle(title);
        slot.setLocationName(locationName);
        slot.setLng(lng);
        slot.setLat(lat);
        slot.setDurationMinutes(duration);
        slot.setPriority(priority != null ? priority : 0);
        return slot;
    }

    private int alignTo30(int minutes) {
        return Math.max(30, ((minutes + 29) / 30) * 30);
    }

    private LocalTime parseTimeSafe(String timeStr, LocalTime defaultValue) {
        try {
            return StrUtil.isNotBlank(timeStr) ? LocalTime.parse(timeStr, TIME_FORMATTER) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void matchTeamCapability(InspectionTeam team, SmartInspectionScheduleDTO.TeamTaskGroup group,
                                     List<AdvancedInspectionScheduleDTO.SchedulingConflictVO> conflicts) {
        if (CollUtil.isEmpty(group.getOrderedTasks())) return;
        Set<String> teamCapSet = StrUtil.isNotBlank(team.getCapabilities()) ?
                new HashSet<>(Arrays.asList(team.getCapabilities().split(","))) : new HashSet<>();
        if (teamCapSet.isEmpty()) {
            AdvancedInspectionScheduleDTO.SchedulingConflictVO c = new AdvancedInspectionScheduleDTO.SchedulingConflictVO();
            c.setConflictType(1);
            c.setConflictTypeName(CONFLICT_TYPE_NAMES[1]);
            c.setSeverity(2);
            c.setSeverityName(SEVERITY_NAMES[2]);
            c.setTeamId(team.getId());
            c.setTeamName(team.getTeamName());
            c.setDescription("班组能力未配置");
            c.setSuggestion("配置班组巡检能力");
            conflicts.add(c);
        }
    }

    private void checkOvertime(InspectionTeam team, SmartInspectionScheduleDTO.TeamTaskGroup group,
                               List<AdvancedInspectionScheduleDTO.SchedulingConflictVO> conflicts) {
        BigDecimal hours = group.getEstimatedHours();
        BigDecimal maxHours = team.getMaxDailyHours() != null ? team.getMaxDailyHours() : BigDecimal.valueOf(8);
        if (hours != null && hours.compareTo(maxHours) > 0) {
            AdvancedInspectionScheduleDTO.SchedulingConflictVO c = new AdvancedInspectionScheduleDTO.SchedulingConflictVO();
            c.setConflictType(2);
            c.setConflictTypeName(CONFLICT_TYPE_NAMES[2]);
            c.setSeverity(3);
            c.setSeverityName(SEVERITY_NAMES[3]);
            c.setTeamId(team.getId());
            c.setTeamName(team.getTeamName());
            c.setDescription("预计工时(" + hours + "小时)超过班组日最大工时(" + maxHours + "小时)");
            c.setSuggestion("拆分任务或增加班组");
            conflicts.add(c);
        }
        BigDecimal km = group.getTotalDistanceKm();
        BigDecimal maxKm = team.getMaxDailyKm() != null ? team.getMaxDailyKm() : BigDecimal.valueOf(15);
        if (km != null && km.compareTo(maxKm) > 0) {
            AdvancedInspectionScheduleDTO.SchedulingConflictVO c = new AdvancedInspectionScheduleDTO.SchedulingConflictVO();
            c.setConflictType(2);
            c.setConflictTypeName(CONFLICT_TYPE_NAMES[2]);
            c.setSeverity(2);
            c.setSeverityName(SEVERITY_NAMES[2]);
            c.setTeamId(team.getId());
            c.setTeamName(team.getTeamName());
            c.setDescription("预计里程(" + km + "km)超过班组日最大里程(" + maxKm + "km)");
            c.setSuggestion("优化路线或拆分任务");
            conflicts.add(c);
        }
    }

    private AdvancedInspectionScheduleDTO.ResourceGanttChartVO buildGantt(
            List<AdvancedInspectionScheduleDTO.TeamDailySchedule> schedules) {
        AdvancedInspectionScheduleDTO.ResourceGanttChartVO gantt = new AdvancedInspectionScheduleDTO.ResourceGanttChartVO();
        gantt.setTimelineStart("08:30");
        gantt.setTimelineEnd("17:30");
        List<AdvancedInspectionScheduleDTO.ResourceGanttChartVO.TeamGanttEntry> entries = new ArrayList<>();
        for (AdvancedInspectionScheduleDTO.TeamDailySchedule s : schedules) {
            AdvancedInspectionScheduleDTO.ResourceGanttChartVO.TeamGanttEntry entry =
                    new AdvancedInspectionScheduleDTO.ResourceGanttChartVO.TeamGanttEntry();
            entry.setTeamId(s.getTeamId());
            entry.setTeamName(s.getTeamName());
            List<AdvancedInspectionScheduleDTO.ResourceGanttChartVO.GanttTimeBlock> blocks = new ArrayList<>();
            if (CollUtil.isNotEmpty(s.getTimeSlots())) {
                String currentType = null;
                String currentTitle = null;
                String blockStart = null;
                String blockEnd = null;
                for (AdvancedInspectionScheduleDTO.TimeSlotTask slot : s.getTimeSlots()) {
                    String typeKey = slot.getTaskType() + "-" + slot.getTaskTitle();
                    if (!typeKey.equals(currentType)) {
                        if (blockStart != null) {
                            AdvancedInspectionScheduleDTO.ResourceGanttChartVO.GanttTimeBlock block =
                                    new AdvancedInspectionScheduleDTO.ResourceGanttChartVO.GanttTimeBlock();
                            block.setStart(blockStart);
                            block.setEnd(blockEnd);
                            Integer tt = taskTypeFromKey(currentType);
                            block.setTaskType(tt);
                            block.setTaskTypeName(tt != null ? TASK_TYPE_NAMES[tt] : "");
                            block.setTaskTitle(currentTitle);
                            blocks.add(block);
                        }
                        blockStart = slot.getStartTime();
                        blockEnd = slot.getEndTime();
                        currentType = typeKey;
                        currentTitle = slot.getTaskTitle();
                    } else {
                        blockEnd = slot.getEndTime();
                    }
                }
                if (blockStart != null) {
                    AdvancedInspectionScheduleDTO.ResourceGanttChartVO.GanttTimeBlock block =
                            new AdvancedInspectionScheduleDTO.ResourceGanttChartVO.GanttTimeBlock();
                    block.setStart(blockStart);
                    block.setEnd(blockEnd);
                    Integer tt = taskTypeFromKey(currentType);
                    block.setTaskType(tt);
                    block.setTaskTypeName(tt != null ? TASK_TYPE_NAMES[tt] : "");
                    block.setTaskTitle(currentTitle);
                    blocks.add(block);
                }
            }
            entry.setBlocks(blocks);
            entries.add(entry);
        }
        gantt.setTeamEntries(entries);
        return gantt;
    }

    private Integer taskTypeFromKey(String key) {
        if (key == null) return null;
        try {
            return Integer.parseInt(key.split("-")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private AdvancedInspectionScheduleDTO.ResourceUsageVO buildResourceUsage(
            List<InspectionTeam> teams, List<Inspector> inspectors,
            List<InspectionVehicle> vehicles) {
        AdvancedInspectionScheduleDTO.ResourceUsageVO usage = new AdvancedInspectionScheduleDTO.ResourceUsageVO();
        usage.setTotalInspectors(inspectors.size());
        usage.setAssignedInspectors(inspectors.size());
        usage.setInspectorUsageRate(inspectors.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(100));
        usage.setTotalVehicles(vehicles.size());
        usage.setAssignedVehicles(vehicles.size());
        usage.setVehicleUsageRate(vehicles.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(100));
        usage.setHasOvertimeWarning(false);
        usage.setOvertimeTeamNames(new ArrayList<>());
        return usage;
    }

    private void updateOvertimeWarnings(AdvancedInspectionScheduleDTO result,
                                        List<AdvancedInspectionScheduleDTO.TeamDailySchedule> schedules) {
        AdvancedInspectionScheduleDTO.ResourceUsageVO usage = result.getResourceUsage();
        if (usage == null) return;
        List<String> overtimeNames = new ArrayList<>();
        for (AdvancedInspectionScheduleDTO.TeamDailySchedule s : schedules) {
            if (s.getTotalHours() != null && s.getTotalHours().compareTo(BigDecimal.valueOf(8)) > 0) {
                overtimeNames.add(s.getTeamName());
            }
        }
        usage.setOvertimeTeamNames(overtimeNames);
        usage.setHasOvertimeWarning(!overtimeNames.isEmpty());
    }
}
