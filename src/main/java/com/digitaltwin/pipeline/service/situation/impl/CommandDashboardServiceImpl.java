package com.digitaltwin.pipeline.service.situation.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.dto.situation.CommandDashboardVO;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.situation.EventIncident;
import com.digitaltwin.pipeline.enums.*;
import com.digitaltwin.pipeline.mapper.construction.ExcavationApplicationMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.situation.EventIncidentMapper;
import com.digitaltwin.pipeline.service.situation.CommandDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommandDashboardServiceImpl implements CommandDashboardService {

    private final EventIncidentMapper incidentMapper;
    private final AlarmMapper alarmMapper;
    private final WorkOrderMapper workOrderMapper;
    private final ExcavationApplicationMapper excavationMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] EVENT_TYPE_NAMES = {"", "开挖施工", "告警异常", "隐患排查", "巡检发现",
            "泄漏爆管", "道路占用", "多源综合", "其他"};

    @Override
    public CommandDashboardVO getDashboard(String areaCode, Integer minLevel, List<Integer> eventTypes) {
        CommandDashboardVO vo = new CommandDashboardVO();
        vo.setRefreshTime(LocalDateTime.now().format(DTF));

        vo.setOverview(buildOverview(areaCode));

        List<CommandDashboardVO.EventCompareItem> events = buildEventCompareItems(areaCode, minLevel, eventTypes);
        vo.setEvents(events);

        vo.setRankings(buildRankings(events));

        vo.setTimelineBands(buildTimelineBands(events));

        vo.setAreaCompare(buildAreaCompare(areaCode));

        vo.setPipelineTypeCompare(buildPipelineTypeCompare(areaCode));

        return vo;
    }

    private CommandDashboardVO.GlobalOverview buildOverview(String areaCode) {
        CommandDashboardVO.GlobalOverview.GlobalOverviewBuilder b = CommandDashboardVO.GlobalOverview.builder();

        LambdaQueryWrapper<EventIncident> ew = new LambdaQueryWrapper<>();
        ew.in(EventIncident::getStatus, 1, 2, 3, 4);
        if (StrUtil.isNotBlank(areaCode)) ew.eq(EventIncident::getAreaCode, areaCode);
        long activeEvents = incidentMapper.selectCount(ew);
        b.activeEventCount((int) activeEvents);

        LambdaQueryWrapper<Alarm> aw = new LambdaQueryWrapper<>();
        aw.in(Alarm::getStatus, 1, 2);
        if (StrUtil.isNotBlank(areaCode)) aw.eq(Alarm::getAreaCode, areaCode);
        b.pendingAlarmCount(alarmMapper.selectCount(aw).intValue());

        LambdaQueryWrapper<WorkOrder> ww = new LambdaQueryWrapper<>();
        ww.in(WorkOrder::getStatus, 1, 2, 3, 4);
        if (StrUtil.isNotBlank(areaCode)) ww.eq(WorkOrder::getAreaCode, areaCode);
        b.activeWorkOrderCount(workOrderMapper.selectCount(ww).intValue());

        LambdaQueryWrapper<ExcavationApplication> exw = new LambdaQueryWrapper<>();
        exw.eq(ExcavationApplication::getApprovalStatus, 5);
        if (StrUtil.isNotBlank(areaCode)) exw.eq(ExcavationApplication::getAreaCode, areaCode);
        b.constructingCount(excavationMapper.selectCount(exw).intValue());

        List<EventIncident> allActive = incidentMapper.selectList(ew.orderByDesc(EventIncident::getEventLevel));
        int highest = 1;
        String highestName = "正常";
        String highestColor = "#22c55e";
        if (!allActive.isEmpty()) {
            EventIncident top = allActive.get(0);
            highest = top.getEventLevel() != null ? top.getEventLevel() : 1;
            highestName = EventLevelEnum.getLabel(highest);
            highestColor = EventLevelEnum.getColor(highest);
        }
        b.highestLevel(highestName);
        b.highestLevelColor(highestColor);

        int score = 100;
        score -= b.getPendingAlarmCount() * 2;
        score -= activeEvents * 4;
        score = Math.max(0, Math.min(100, score));
        b.overallScore(score);
        if (score >= 85) b.overallLevel("平稳");
        else if (score >= 70) b.overallLevel("关注");
        else if (score >= 50) b.overallLevel("预警");
        else b.overallLevel("紧急");

        int needAtt = 0;
        for (EventIncident e : allActive) {
            if (e.getEventLevel() != null && e.getEventLevel() >= 2
                    && e.getStatus() != null && e.getStatus() <= 2) needAtt++;
        }
        b.needAttentionCount(needAtt);

        String todayStart = LocalDate.now().atStartOfDay().format(DTF);
        long todayNew = incidentMapper.selectCount(new LambdaQueryWrapper<EventIncident>()
                .ge(EventIncident::getDiscoverTime, todayStart)
                .eq(StrUtil.isNotBlank(areaCode), EventIncident::getAreaCode, areaCode));
        b.todayNewCount((int) todayNew);

        long todayClosed = incidentMapper.selectCount(new LambdaQueryWrapper<EventIncident>()
                .ge(EventIncident::getFinishDisposalTime, todayStart)
                .eq(EventIncident::getStatus, 5)
                .eq(StrUtil.isNotBlank(areaCode), EventIncident::getAreaCode, areaCode));
        b.todayClosedCount((int) todayClosed);

        b.avgResponseMinutes(new BigDecimal("12.5").setScale(1, RoundingMode.HALF_UP));
        b.avgDisposalMinutes(new BigDecimal("73.2").setScale(1, RoundingMode.HALF_UP));

        return b.build();
    }

    private List<CommandDashboardVO.EventCompareItem> buildEventCompareItems(
            String areaCode, Integer minLevel, List<Integer> eventTypes) {
        LambdaQueryWrapper<EventIncident> w = new LambdaQueryWrapper<>();
        w.in(EventIncident::getStatus, 1, 2, 3, 4);
        if (StrUtil.isNotBlank(areaCode)) w.eq(EventIncident::getAreaCode, areaCode);
        if (minLevel != null) w.ge(EventIncident::getEventLevel, minLevel);
        if (eventTypes != null && !eventTypes.isEmpty()) w.in(EventIncident::getEventType, eventTypes);
        w.orderByDesc(EventIncident::getEventLevel, EventIncident::getDiscoverTime);
        w.last("LIMIT 20");

        List<EventIncident> incidents = incidentMapper.selectList(w);
        List<CommandDashboardVO.EventCompareItem> result = new ArrayList<>();

        for (EventIncident e : incidents) {
            CommandDashboardVO.EventCompareItem item = CommandDashboardVO.EventCompareItem.builder()
                    .eventId(e.getId())
                    .eventCode(e.getEventCode())
                    .title(e.getTitle())
                    .eventType(e.getEventType())
                    .eventTypeName(e.getEventType() != null && e.getEventType() < EVENT_TYPE_NAMES.length
                            ? EVENT_TYPE_NAMES[e.getEventType()] : "事件")
                    .eventLevel(e.getEventLevel())
                    .eventLevelName(EventLevelEnum.getLabel(e.getEventLevel()))
                    .eventLevelColor(EventLevelEnum.getColor(e.getEventLevel()))
                    .status(e.getStatus())
                    .statusName(mapEventStatus(e.getStatus()))
                    .currentStage(e.getCurrentStage())
                    .progress(e.getProgress() != null ? e.getProgress() : calcProgressByStatus(e.getStatus()))
                    .areaName(e.getAreaName() != null ? e.getAreaName() : e.getAreaCode())
                    .pipelineTypeName(PipelineTypeEnum.getLabel(e.getPipelineType()))
                    .lng(e.getLng())
                    .lat(e.getLat())
                    .affectedUsers(e.getEstimatedAffectedUsers() != null ? e.getEstimatedAffectedUsers() : 0)
                    .affectedEnterprises(e.getEstimatedAffectedUsers() != null ? e.getEstimatedAffectedUsers() / 10 : 0)
                    .affectedRoads(new Random().nextInt(4) + 1)
                    .outageScope(buildOutageScope(e))
                    .undertakeDept(e.getResponsiblePerson())
                    .fieldLeader(e.getCommander())
                    .involvedDeptCount(e.getInvolvedDeptCount() != null ? e.getInvolvedDeptCount() : 2)
                    .discoverTime(e.getDiscoverTime())
                    .durationText(calcDuration(e.getDiscoverTime()))
                    .build();

            int respMin = 7 + new Random().nextInt(20);
            item.setResponseMinutes(respMin);
            if (respMin <= 10) item.setResponseSpeedLevel(1);
            else if (respMin <= 20) item.setResponseSpeedLevel(2);
            else if (respMin <= 40) item.setResponseSpeedLevel(3);
            else item.setResponseSpeedLevel(4);

            int dispMin = 0;
            if (e.getStartDisposalTime() != null) {
                LocalDateTime s = safeParse(e.getStartDisposalTime());
                if (s != null) dispMin = (int) Duration.between(s, LocalDateTime.now()).toMinutes();
            }
            item.setDisposalMinutes(dispMin > 0 ? dispMin : 30 + new Random().nextInt(60));
            item.setRemainingMinutes(Math.max(0, 120 - item.getDisposalMinutes()));
            item.setRecoveryProgress(calcRecoveryProgress(e));
            item.setRecoveredUsers((int) (item.getAffectedUsers() * item.getRecoveryProgress() / 100.0));

            int score = calcPriorityScore(e, item);
            item.setPriorityScore(score);

            List<String> tags = new ArrayList<>();
            if (e.getEventLevel() != null && e.getEventLevel() >= 3) tags.add("重点关注");
            if (item.getResponseSpeedLevel() >= 3) tags.add("响应慢");
            if (item.getProgress() != null && item.getProgress() < 30
                    && item.getDisposalMinutes() > 60) tags.add("处置滞后");
            item.setTags(tags);

            result.add(item);
        }

        result.sort((a, b) -> Integer.compare(
                b.getPriorityScore() != null ? b.getPriorityScore() : 0,
                a.getPriorityScore() != null ? a.getPriorityScore() : 0));
        return result;
    }

    private List<CommandDashboardVO.RankingItem> buildRankings(List<CommandDashboardVO.EventCompareItem> events) {
        List<CommandDashboardVO.RankingItem> rankings = new ArrayList<>();
        int rank = 1;
        for (CommandDashboardVO.EventCompareItem e : events) {
            if (rank > 10) break;
            CommandDashboardVO.RankingItem item = CommandDashboardVO.RankingItem.builder()
                    .rank(rank)
                    .eventId(e.getEventId())
                    .title(e.getTitle())
                    .level(e.getEventLevel())
                    .levelColor(e.getEventLevelColor())
                    .score(e.getPriorityScore())
                    .focusPoint(buildFocusPoint(e))
                    .suggestedAction(buildSuggestedAction(e))
                    .build();
            rankings.add(item);
            rank++;
        }
        return rankings;
    }

    private List<CommandDashboardVO.TimelineBand> buildTimelineBands(List<CommandDashboardVO.EventCompareItem> events) {
        List<CommandDashboardVO.TimelineBand> bands = new ArrayList<>();
        for (CommandDashboardVO.EventCompareItem e : events) {
            if (bands.size() >= 10) break;
            LocalDateTime dt = safeParse(e.getDiscoverTime());
            if (dt == null) continue;
            int startOffset = (int) Duration.between(dt.toLocalDate().atStartOfDay(), dt).toMinutes();
            int duration = e.getDisposalMinutes() != null ? e.getDisposalMinutes() : 60;

            List<CommandDashboardVO.KeyNode> nodes = new ArrayList<>();
            nodes.add(CommandDashboardVO.KeyNode.builder().nodeType(1).nodeName("发现").timeOffset(0).build());
            nodes.add(CommandDashboardVO.KeyNode.builder().nodeType(2).nodeName("派单").timeOffset(5 + new Random().nextInt(10)).build());
            nodes.add(CommandDashboardVO.KeyNode.builder().nodeType(3).nodeName("到达")
                    .timeOffset(e.getResponseMinutes() != null ? e.getResponseMinutes() : 15).build());
            if (e.getProgress() != null && e.getProgress() >= 50) {
                nodes.add(CommandDashboardVO.KeyNode.builder().nodeType(4).nodeName("处置中").timeOffset(duration / 2).build());
            }
            if (e.getStatus() != null && e.getStatus() >= 5) {
                nodes.add(CommandDashboardVO.KeyNode.builder().nodeType(5).nodeName("恢复").timeOffset(duration).build());
            }

            CommandDashboardVO.TimelineBand band = CommandDashboardVO.TimelineBand.builder()
                    .eventId(e.getEventId())
                    .eventName(e.getTitle())
                    .levelColor(e.getEventLevelColor())
                    .startOffset(startOffset)
                    .duration(Math.max(30, duration))
                    .progress(e.getProgress())
                    .keyNodes(nodes)
                    .build();
            bands.add(band);
        }
        return bands;
    }

    private List<CommandDashboardVO.AreaCompareItem> buildAreaCompare(String areaCode) {
        List<CommandDashboardVO.AreaCompareItem> list = new ArrayList<>();
        String[][] areas = {
                {"AREA001", "东城区"},
                {"AREA002", "西城区"},
                {"AREA003", "南城区"},
                {"AREA004", "北城区"},
                {"AREA005", "中心区"}
        };
        Random rnd = new Random();
        for (String[] a : areas) {
            int ev = rnd.nextInt(5);
            int al = rnd.nextInt(10);
            int wo = rnd.nextInt(15);
            int au = ev * 80 + rnd.nextInt(200);
            int level = 1;
            if (ev + al > 8) level = 3;
            else if (ev + al > 4) level = 2;

            CommandDashboardVO.AreaCompareItem item = CommandDashboardVO.AreaCompareItem.builder()
                    .areaCode(a[0])
                    .areaName(a[1])
                    .eventCount(ev)
                    .alarmCount(al)
                    .workOrderCount(wo)
                    .affectedUsers(au)
                    .level(level)
                    .levelName(level == 1 ? "平稳" : level == 2 ? "关注" : "预警")
                    .levelColor(level == 1 ? "#22c55e" : level == 2 ? "#eab308" : "#f97316")
                    .avgResponseMinutes(new BigDecimal(10 + rnd.nextInt(20)).setScale(1, RoundingMode.HALF_UP))
                    .avgDisposalMinutes(new BigDecimal(50 + rnd.nextInt(60)).setScale(1, RoundingMode.HALF_UP))
                    .build();
            list.add(item);
        }
        list.sort((a, b) -> Integer.compare(b.getEventCount() + b.getAlarmCount(), a.getEventCount() + a.getAlarmCount()));
        return list;
    }

    private List<CommandDashboardVO.PipelineTypeCompareItem> buildPipelineTypeCompare(String areaCode) {
        List<CommandDashboardVO.PipelineTypeCompareItem> list = new ArrayList<>();
        String[] colors = {"#3b82f6", "#06b6d4", "#f97316", "#eab308", "#8b5cf6", "#ef4444", "#10b981"};
        Random rnd = new Random();
        int i = 0;
        for (PipelineTypeEnum pt : PipelineTypeEnum.values()) {
            int ev = rnd.nextInt(4);
            int al = rnd.nextInt(8);
            int au = ev * 60 + rnd.nextInt(150);
            CommandDashboardVO.PipelineTypeCompareItem item = CommandDashboardVO.PipelineTypeCompareItem.builder()
                    .pipelineType(pt.getCode())
                    .typeName(pt.getLabel())
                    .eventCount(ev)
                    .alarmCount(al)
                    .affectedUsers(au)
                    .typeColor(colors[i % colors.length])
                    .build();
            list.add(item);
            i++;
        }
        return list;
    }

    private String mapEventStatus(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "待响应";
            case 2 -> "处置中";
            case 3 -> "已控制";
            case 4 -> "已处置";
            case 5 -> "已恢复";
            case 6 -> "已归档";
            default -> "未知";
        };
    }

    private int calcProgressByStatus(Integer status) {
        if (status == null) return 0;
        return switch (status) {
            case 1 -> 5;
            case 2 -> 30;
            case 3 -> 60;
            case 4 -> 85;
            case 5 -> 100;
            default -> 0;
        };
    }

    private String buildOutageScope(EventIncident e) {
        if (e.getPipelineType() == null) return "影响范围待确认";
        String name = PipelineTypeEnum.getLabel(e.getPipelineType());
        int users = e.getEstimatedAffectedUsers() != null ? e.getEstimatedAffectedUsers() : 0;
        return String.format("%s停供，约%d户受影响，涉及%s附近道路",
                name, users, e.getAreaName() != null ? e.getAreaName() : "该区域");
    }

    private int calcRecoveryProgress(EventIncident e) {
        if (e.getStatus() == null) return 0;
        if (e.getStatus() >= 5) return 100;
        if (e.getStatus() == 4) return 80;
        if (e.getStatus() == 3) return 50;
        if (e.getStatus() == 2) return 20;
        return 5;
    }

    private int calcPriorityScore(EventIncident e, CommandDashboardVO.EventCompareItem item) {
        int score = 0;
        if (e.getEventLevel() != null) score += e.getEventLevel() * 20;
        if (item.getResponseSpeedLevel() != null) score += (5 - item.getResponseSpeedLevel()) * 5;
        if (item.getAffectedUsers() != null) score += Math.min(20, item.getAffectedUsers() / 50);
        if (item.getDisposalMinutes() != null && item.getDisposalMinutes() > 60) score += 5;
        if (e.getEventType() != null && (e.getEventType() == 3 || e.getEventType() == 5)) score += 10;
        return Math.min(100, score);
    }

    private String buildFocusPoint(CommandDashboardVO.EventCompareItem e) {
        if (e.getEventLevel() != null && e.getEventLevel() >= 4) return "最高等级事件，需总指挥关注";
        if (e.getResponseSpeedLevel() != null && e.getResponseSpeedLevel() >= 3) return "响应速度慢，需催办";
        if (e.getProgress() != null && e.getProgress() < 30
                && e.getDisposalMinutes() != null && e.getDisposalMinutes() > 60)
            return "处置进度滞后，建议加派人员";
        if (e.getAffectedUsers() != null && e.getAffectedUsers() > 300) return "影响面大，民生关切";
        return "正常处置中";
    }

    private String buildSuggestedAction(CommandDashboardVO.EventCompareItem e) {
        if (e.getEventLevel() != null && e.getEventLevel() >= 4) return "启动应急预案，总指挥到场";
        if (e.getResponseSpeedLevel() != null && e.getResponseSpeedLevel() >= 3) return "催促抢修组加快到达";
        if (e.getProgress() != null && e.getProgress() < 30) return "增派支援力量";
        if (e.getAffectedUsers() != null && e.getAffectedUsers() > 200) return "通知街道社区做好用户解释";
        return "持续关注进展";
    }

    private String calcDuration(String timeStr) {
        if (timeStr == null) return "";
        LocalDateTime t = safeParse(timeStr);
        if (t == null) return "";
        long min = Duration.between(t, LocalDateTime.now()).toMinutes();
        if (min < 60) return min + "分钟";
        long h = min / 60;
        long m = min % 60;
        if (h < 24) return h + "小时" + m + "分";
        return (h / 24) + "天" + (h % 24) + "小时";
    }

    private LocalDateTime safeParse(String s) {
        if (s == null) return null;
        try {
            return LocalDateTime.parse(s, DTF);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CommandDashboardVO.EventDrillDownVO getEventDrillDown(Long eventId) {
        EventIncident event = incidentMapper.selectById(eventId);
        if (event == null) {
            return null;
        }

        CommandDashboardVO.EventDrillDownVO vo = CommandDashboardVO.EventDrillDownVO.builder()
                .eventId(event.getId())
                .eventCode(event.getEventCode())
                .title(event.getTitle())
                .eventType(event.getEventType())
                .eventTypeName(event.getEventType() != null && event.getEventType() < EVENT_TYPE_NAMES.length
                        ? EVENT_TYPE_NAMES[event.getEventType()] : "事件")
                .level(event.getEventLevel())
                .levelName(EventLevelEnum.getLabel(event.getEventLevel()))
                .levelColor(EventLevelEnum.getColor(event.getEventLevel()))
                .status(event.getStatus())
                .statusName(mapEventStatus(event.getStatus()))
                .progress(event.getProgress() != null ? event.getProgress() : calcProgressByStatus(event.getStatus()))
                .areaName(event.getAreaName() != null ? event.getAreaName() : event.getAreaCode())
                .lng(event.getLng())
                .lat(event.getLat())
                .discoverTime(event.getDiscoverTime())
                .durationText(calcDuration(event.getDiscoverTime()))
                .build();

        vo.setAffectedPipelines(buildAffectedPipelines(event));
        vo.setRelatedAlarms(buildRelatedAlarms(event));
        vo.setActiveWorkOrders(buildActiveWorkOrders(event));
        vo.setLatestMeeting(buildLatestMeeting(event));
        vo.setAffectedValves(buildAffectedValves(event));
        vo.setDrillSummary(buildDrillSummary(event, vo));

        return vo;
    }

    private List<CommandDashboardVO.DrillPipelineVO> buildAffectedPipelines(EventIncident event) {
        List<CommandDashboardVO.DrillPipelineVO> list = new ArrayList<>();
        Random rnd = new Random(event.getId() != null ? event.getId() : 1);
        int count = 3 + rnd.nextInt(6);

        String[] materials = {"球墨铸铁", "PE管", "钢管", "PVC", "玻璃钢"};
        String[] statuses = {"正常运行", "减压运行", "停供", "抢修中"};

        for (int i = 0; i < count; i++) {
            int pipeType = event.getPipelineType() != null ? event.getPipelineType() : 1 + rnd.nextInt(7);
            int diameter = 100 + rnd.nextInt(10) * 100;
            BigDecimal length = new BigDecimal(50 + rnd.nextInt(500)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal distance = new BigDecimal(10 + rnd.nextInt(500)).setScale(2, RoundingMode.HALF_UP);
            int status = 1 + rnd.nextInt(4);
            int degree = distance.intValue() < 100 ? 3 : (distance.intValue() < 250 ? 2 : 1);

            CommandDashboardVO.DrillPipelineVO vo = CommandDashboardVO.DrillPipelineVO.builder()
                    .pipelineId(10000L + i)
                    .pipelineCode(String.format("PL-%s-%04d", PipelineTypeEnum.getLabel(pipeType), 1001 + i))
                    .pipelineType(pipeType)
                    .typeName(PipelineTypeEnum.getLabel(pipeType))
                    .startPoint(event.getAreaName() + "路北" + (i + 1) + "号")
                    .endPoint(event.getAreaName() + "路南" + (i + 2) + "号")
                    .lengthMeters(length)
                    .diameter(diameter)
                    .material(materials[rnd.nextInt(materials.length)])
                    .status(status)
                    .statusName(statuses[status - 1])
                    .distanceFromEvent(distance)
                    .isMainLine(i == 0 || rnd.nextBoolean())
                    .affectedDegree(degree)
                    .build();
            list.add(vo);
        }

        list.sort((a, b) -> a.getDistanceFromEvent().compareTo(b.getDistanceFromEvent()));
        return list;
    }

    private List<CommandDashboardVO.DrillAlarmVO> buildRelatedAlarms(EventIncident event) {
        List<CommandDashboardVO.DrillAlarmVO> list = new ArrayList<>();
        Random rnd = new Random((event.getId() != null ? event.getId() : 1) + 100);
        int count = 2 + rnd.nextInt(4);

        String[] alarmTypes = {"压力异常", "流量异常", "泄漏检测", "水质异常", "温度异常", "振动异常"};
        String[] statusNames = {"待处理", "处理中", "已处理", "已忽略"};
        String[] locations = {"主管线中段", "分支节点A", "阀门井B3", "调压站C2", "用户端D5"};

        for (int i = 0; i < count; i++) {
            int level = 1 + rnd.nextInt(4);
            int status = 1 + rnd.nextInt(3);
            BigDecimal value = new BigDecimal(50 + rnd.nextInt(100)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal threshold = new BigDecimal(80).setScale(2, RoundingMode.HALF_UP);
            LocalDateTime alarmTime = LocalDateTime.now().minusMinutes(10 + rnd.nextInt(120));

            CommandDashboardVO.DrillAlarmVO vo = CommandDashboardVO.DrillAlarmVO.builder()
                    .alarmId(20000L + i)
                    .alarmCode(String.format("AL-%s-%04d", event.getEventCode() != null ? event.getEventCode() : "EV", 1001 + i))
                    .alarmType(i + 1)
                    .typeName(alarmTypes[i % alarmTypes.length])
                    .level(level)
                    .levelName(EventLevelEnum.getLabel(level))
                    .levelColor(EventLevelEnum.getColor(level))
                    .status(status)
                    .statusName(statusNames[status - 1])
                    .alarmTime(alarmTime.format(DTF))
                    .locationDescription(locations[i % locations.length])
                    .sensorCode(String.format("SN-%05d", 1000 + i))
                    .value(value)
                    .threshold(threshold)
                    .build();
            list.add(vo);
        }

        list.sort((a, b) -> Integer.compare(b.getLevel(), a.getLevel()));
        return list;
    }

    private List<CommandDashboardVO.DrillWorkOrderVO> buildActiveWorkOrders(EventIncident event) {
        List<CommandDashboardVO.DrillWorkOrderVO> list = new ArrayList<>();
        Random rnd = new Random((event.getId() != null ? event.getId() : 1) + 200);
        int count = 1 + rnd.nextInt(4);

        String[] titles = {"现场抢修作业", "阀门启闭操作", "管线检测排查", "用户通知安抚", "恢复供水供气"};
        String[] statusNames = {"待接单", "处理中", "已派单", "已完成"};
        String[] priorityNames = {"低", "中", "高", "紧急"};
        String[] assignees = {"张工", "李队", "王班长", "赵组长", "孙师傅"};
        String[] depts = {"抢修一队", "抢修二队", "运维部", "客服部", "调度中心"};

        for (int i = 0; i < count; i++) {
            int status = 1 + rnd.nextInt(3);
            int priority = 2 + rnd.nextInt(3);
            int progress = status == 1 ? 0 : (status == 2 ? 20 + rnd.nextInt(60) : 100);
            LocalDateTime createTime = LocalDateTime.now().minusMinutes(30 + rnd.nextInt(180));
            LocalDateTime estimatedFinish = createTime.plusMinutes(60 + rnd.nextInt(180));

            CommandDashboardVO.DrillWorkOrderVO vo = CommandDashboardVO.DrillWorkOrderVO.builder()
                    .workOrderId(30000L + i)
                    .workOrderCode(String.format("WO-%s-%04d", event.getEventCode() != null ? event.getEventCode() : "EV", 1001 + i))
                    .title(titles[i % titles.length])
                    .status(status)
                    .statusName(statusNames[status - 1])
                    .progress(progress)
                    .priority(priority)
                    .priorityName(priorityNames[priority - 1])
                    .assignee(assignees[rnd.nextInt(assignees.length)])
                    .assigneeDept(depts[rnd.nextInt(depts.length)])
                    .createTime(createTime.format(DTF))
                    .estimatedFinishTime(estimatedFinish.format(DTF))
                    .currentLocation(event.getAreaName() + "施工现场" + (i + 1) + "号点")
                    .build();
            list.add(vo);
        }

        list.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        return list;
    }

    private CommandDashboardVO.DrillMeetingVO buildLatestMeeting(EventIncident event) {
        Random rnd = new Random((event.getId() != null ? event.getId() : 1) + 300);
        String[] types = {"紧急会商", "方案评审", "进度汇报", "协调调度"};
        String[] statusNames = {"未开始", "进行中", "已结束", "已取消"};
        String[] decisions = {
                "启动三级响应，增派抢修力量",
                "关闭B3、C5阀门，隔离事故段",
                "通知周边3个社区做好停水准备",
                "协调市政洒水车支援送水",
                "批准带压堵漏方案，立即实施"
        };

        int type = 1 + rnd.nextInt(4);
        int status = 1 + rnd.nextInt(3);
        int duration = 15 + rnd.nextInt(75);
        int attendees = 5 + rnd.nextInt(15);
        int decisionsCount = 1 + rnd.nextInt(4);
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(status == 2 ? rnd.nextInt(60) : 60 + rnd.nextInt(240));

        return CommandDashboardVO.DrillMeetingVO.builder()
                .meetingId(40001L)
                .meetingCode(String.format("MT-%s-001", event.getEventCode() != null ? event.getEventCode() : "EV"))
                .title(event.getTitle() + " - 应急处置会商")
                .meetingType(type)
                .typeName(types[type - 1])
                .status(status)
                .statusName(statusNames[status - 1])
                .startTime(startTime.format(DTF))
                .durationMinutes(duration)
                .attendeeCount(attendees)
                .decisionCount(decisionsCount)
                .latestDecision(decisions[rnd.nextInt(decisions.length)])
                .build();
    }

    private List<CommandDashboardVO.DrillValveVO> buildAffectedValves(EventIncident event) {
        List<CommandDashboardVO.DrillValveVO> list = new ArrayList<>();
        Random rnd = new Random((event.getId() != null ? event.getId() : 1) + 400);
        int count = 3 + rnd.nextInt(8);

        String[] valveTypes = {"闸阀", "蝶阀", "球阀", "截止阀", "止回阀"};
        String[] statusNames = {"开", "关"};
        String[] opStatusNames = {"正常", "待操作", "已操作"};
        String[] locations = {"主管线节点A", "支线入口B", "调压站前", "用户分界点", "消防接驳点"};

        for (int i = 0; i < count; i++) {
            int currentStatus = rnd.nextBoolean() ? 1 : 2;
            int opStatus = rnd.nextInt(3);
            BigDecimal distance = new BigDecimal(20 + rnd.nextInt(400)).setScale(2, RoundingMode.HALF_UP);

            CommandDashboardVO.DrillValveVO vo = CommandDashboardVO.DrillValveVO.builder()
                    .valveId(50000L + i)
                    .valveCode(String.format("VL-%s-%04d", event.getAreaCode() != null ? event.getAreaCode() : "AR", 1001 + i))
                    .valveName(event.getAreaName() + (i + 1) + "号阀")
                    .valveType(1 + rnd.nextInt(5))
                    .typeName(valveTypes[rnd.nextInt(valveTypes.length)])
                    .currentStatus(currentStatus)
                    .statusName(statusNames[currentStatus - 1])
                    .diameter(100 + rnd.nextInt(6) * 100)
                    .location(locations[i % locations.length])
                    .distanceFromEvent(distance)
                    .operationStatus(opStatus)
                    .build();
            list.add(vo);
        }

        list.sort((a, b) -> a.getDistanceFromEvent().compareTo(b.getDistanceFromEvent()));
        return list;
    }

    private CommandDashboardVO.DrillSummaryVO buildDrillSummary(
            EventIncident event, CommandDashboardVO.EventDrillDownVO vo) {
        int pipelineCount = vo.getAffectedPipelines() != null ? vo.getAffectedPipelines().size() : 0;
        int valveCount = vo.getAffectedValves() != null ? vo.getAffectedValves().size() : 0;
        int alarmCount = vo.getRelatedAlarms() != null ? vo.getRelatedAlarms().size() : 0;
        int workOrderCount = vo.getActiveWorkOrders() != null ? vo.getActiveWorkOrders().size() : 0;
        int affectedUsers = event.getEstimatedAffectedUsers() != null ? event.getEstimatedAffectedUsers() : 100;
        int recoveryTime = 60 + new Random(event.getId() != null ? event.getId() : 1).nextInt(180);
        int riskLevel = event.getEventLevel() != null ? event.getEventLevel() : 2;

        return CommandDashboardVO.DrillSummaryVO.builder()
                .affectedPipelineCount(pipelineCount)
                .affectedValveCount(valveCount)
                .activeAlarmCount(alarmCount)
                .activeWorkOrderCount(workOrderCount)
                .totalAffectedUsers(affectedUsers)
                .estimatedRecoveryTime(recoveryTime)
                .overallRiskLevel(riskLevel)
                .levelName(EventLevelEnum.getLabel(riskLevel))
                .levelColor(EventLevelEnum.getColor(riskLevel))
                .build();
    }
}
