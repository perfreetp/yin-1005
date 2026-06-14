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
}
