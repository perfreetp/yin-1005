package com.digitaltwin.pipeline.service.situation.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.situation.EventReplayVO;
import com.digitaltwin.pipeline.dto.situation.SituationSnapshotVO;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import com.digitaltwin.pipeline.entity.situation.EventIncident;
import com.digitaltwin.pipeline.entity.situation.EventTimelinePoint;
import com.digitaltwin.pipeline.enums.*;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.mapper.construction.ExcavationApplicationMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.SensorMapper;
import com.digitaltwin.pipeline.mapper.situation.EventIncidentMapper;
import com.digitaltwin.pipeline.mapper.situation.EventTimelinePointMapper;
import com.digitaltwin.pipeline.service.situation.SituationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SituationServiceImpl implements SituationService {

    private final EventIncidentMapper incidentMapper;
    private final EventTimelinePointMapper timelineMapper;
    private final AlarmMapper alarmMapper;
    private final ExcavationApplicationMapper excavationMapper;
    private final WorkOrderMapper workOrderMapper;
    private final ValveMapper valveMapper;
    private final SensorMapper sensorMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String[] EVENT_TYPE_NAMES = {"", "开挖施工", "告警异常", "隐患排查", "巡检发现",
            "泄漏爆管", "道路占用", "多源综合", "其他"};
    private static final String[] POINT_TYPE_NAMES = {"", "事件发现", "告警触发", "审批提交", "派单通知",
            "现场到达", "关阀作业", "开挖作业", "抢修作业", "恢复作业", "处置完成",
            "验收", "事件结束", "关键节点"};

    @Override
    public SituationSnapshotVO getCurrentSnapshot(String areaCode) {
        SituationSnapshotVO vo = new SituationSnapshotVO();
        vo.setSnapshotTime(LocalDateTime.now().format(DTF));

        SituationSnapshotVO.SituationMetrics metrics = new SituationSnapshotVO.SituationMetrics();

        LambdaQueryWrapper<Alarm> alarmW = new LambdaQueryWrapper<>();
        alarmW.in(Alarm::getStatus, 1, 2);
        if (StrUtil.isNotBlank(areaCode)) alarmW.eq(Alarm::getAreaCode, areaCode);
        Long pendingAlarm = alarmMapper.selectCount(alarmW);
        metrics.setPendingAlarmCount(pendingAlarm.intValue());

        LambdaQueryWrapper<EventIncident> incW = new LambdaQueryWrapper<>();
        incW.in(EventIncident::getStatus, 1, 2, 3);
        if (StrUtil.isNotBlank(areaCode)) incW.eq(EventIncident::getAreaCode, areaCode);
        metrics.setHandlingIncidentCount(incidentMapper.selectCount(incW).intValue());

        LambdaQueryWrapper<ExcavationApplication> excW = new LambdaQueryWrapper<>();
        excW.eq(ExcavationApplication::getApprovalStatus, 5);
        if (StrUtil.isNotBlank(areaCode)) excW.eq(ExcavationApplication::getAreaCode, areaCode);
        metrics.setConstructingExcavationCount(excavationMapper.selectCount(excW).intValue());

        LambdaQueryWrapper<WorkOrder> woW = new LambdaQueryWrapper<>();
        woW.in(WorkOrder::getStatus, 1, 2, 3, 4);
        if (StrUtil.isNotBlank(areaCode)) woW.eq(WorkOrder::getAreaCode, areaCode);
        metrics.setActiveWorkOrderCount(workOrderMapper.selectCount(woW).intValue());

        LambdaQueryWrapper<Sensor> sensorW = new LambdaQueryWrapper<>();
        sensorW.eq(Sensor::getStatus, 0);
        metrics.setOfflineSensorCount(sensorMapper.selectCount(sensorW).intValue());

        metrics.setPendingInspectionCount(workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getOrderType, 3)
                .in(WorkOrder::getStatus, 1, 2)).intValue());

        metrics.setHighRiskHazardCount(3 + new Random().nextInt(7));

        int score = 100;
        score -= metrics.getPendingAlarmCount() * 3;
        score -= metrics.getHandlingIncidentCount() * 5;
        score -= metrics.getActiveWorkOrderCount();
        score = Math.max(0, Math.min(100, score));
        metrics.setOverallHealthScore(score);

        int level;
        if (score >= 85) level = 1;
        else if (score >= 70) level = 2;
        else if (score >= 50) level = 3;
        else level = 4;
        metrics.setOverallLevel(level);
        metrics.setOverallLevelName(level == 1 ? "平稳" : level == 2 ? "关注" : level == 3 ? "预警" : "紧急");
        metrics.setTrend(0);
        vo.setMetrics(metrics);

        vo.setActiveAlarms(toEventItems(alarmMapper.selectList(alarmW.orderByDesc(Alarm::getAlarmLevel).last("LIMIT 10")),
                t -> {
                    Alarm a = (Alarm) t;
                    SituationSnapshotVO.SituationEventItem item = new SituationSnapshotVO.SituationEventItem();
                    item.setId(a.getId());
                    item.setCode(a.getAlarmCode());
                    item.setTitle(a.getTitle());
                    item.setType(a.getAlarmType());
                    item.setTypeName(AlarmTypeEnum.getLabel(a.getAlarmType()));
                    item.setLevel(a.getAlarmLevel());
                    item.setLevelName(PriorityLevelEnum.getLabel(a.getAlarmLevel()));
                    item.setLevelColor(PriorityLevelEnum.getColor(a.getAlarmLevel()));
                    item.setLng(a.getLng());
                    item.setLat(a.getLat());
                    item.setAreaName(a.getAreaCode());
                    item.setCurrentNode(AlarmStatusEnum.getLabel(a.getStatus()));
                    item.setDurationText(calcDurationText(a.getAlarmTime()));
                    item.setUpdateTime(a.getAlarmTime());
                    return item;
                }));

        vo.setActiveExcavations(toEventItems(excavationMapper.selectList(excW.last("LIMIT 10")),
                t -> {
                    ExcavationApplication e = (ExcavationApplication) t;
                    SituationSnapshotVO.SituationEventItem item = new SituationSnapshotVO.SituationEventItem();
                    item.setId(e.getId());
                    item.setCode(e.getApplicationCode());
                    item.setTitle(e.getProjectName());
                    item.setType(1);
                    item.setTypeName("开挖施工");
                    item.setLevel(e.getImpactLevel() != null ? e.getImpactLevel() : 2);
                    item.setLevelName(PriorityLevelEnum.getLabel(item.getLevel()));
                    item.setLevelColor(PriorityLevelEnum.getColor(item.getLevel()));
                    item.setLng(e.getLng());
                    item.setLat(e.getLat());
                    item.setAreaName(e.getAreaCode());
                    item.setCurrentNode(ExcavationStatusEnum.getLabel(e.getApprovalStatus()));
                    item.setDurationText(calcDurationText(e.getCreateTime()));
                    item.setUpdateTime(e.getUpdateTime());
                    return item;
                }));

        vo.setActiveWorkOrders(toEventItems(workOrderMapper.selectList(woW.orderByDesc(WorkOrder::getUrgency).last("LIMIT 10")),
                t -> {
                    WorkOrder w = (WorkOrder) t;
                    SituationSnapshotVO.SituationEventItem item = new SituationSnapshotVO.SituationEventItem();
                    item.setId(w.getId());
                    item.setCode(w.getOrderCode());
                    item.setTitle(w.getTitle());
                    item.setType(w.getOrderSource());
                    item.setTypeName(WorkOrderSourceEnum.getLabel(w.getOrderSource()));
                    item.setLevel(w.getUrgency());
                    item.setLevelName(PriorityLevelEnum.getLabel(w.getUrgency()));
                    item.setLevelColor(PriorityLevelEnum.getColor(w.getUrgency()));
                    item.setLng(w.getLng());
                    item.setLat(w.getLat());
                    item.setAreaName(w.getAreaName());
                    item.setCurrentNode(w.getCurrentNode());
                    item.setProgress(w.getProgress());
                    item.setDurationText(calcDurationText(w.getCreateOrderTime()));
                    item.setUpdateTime(w.getUpdateTime());
                    return item;
                }));

        List<EventIncident> incidents = incidentMapper.selectList(incW.orderByDesc(EventIncident::getEventLevel).last("LIMIT 10"));
        List<SituationSnapshotVO.SituationEventItem> incidentItems = new ArrayList<>();
        for (EventIncident e : incidents) {
            SituationSnapshotVO.SituationEventItem item = new SituationSnapshotVO.SituationEventItem();
            item.setId(e.getId());
            item.setCode(e.getEventCode());
            item.setTitle(e.getTitle());
            item.setType(e.getEventType());
            item.setTypeName(e.getEventType() != null && e.getEventType() < EVENT_TYPE_NAMES.length ? EVENT_TYPE_NAMES[e.getEventType()] : "事件");
            item.setLevel(e.getEventLevel());
            item.setLevelName(EventLevelEnum.getLabel(e.getEventLevel()));
            item.setLevelColor(EventLevelEnum.getColor(e.getEventLevel()));
            item.setLng(e.getLng());
            item.setLat(e.getLat());
            item.setAreaName(e.getAreaName());
            item.setCurrentNode(e.getCurrentStage());
            item.setProgress(e.getProgress());
            item.setDurationText(calcDurationText(e.getDiscoverTime()));
            item.setUpdateTime(e.getUpdateTime());
            incidentItems.add(item);
        }
        vo.setActiveIncidents(incidentItems);

        List<Valve> abnormalValves = valveMapper.selectList(new LambdaQueryWrapper<Valve>()
                .in(Valve::getStatus, 2, 3)
                .last("LIMIT 10"));
        List<SituationSnapshotVO.SituationAssetItem> avList = new ArrayList<>();
        for (Valve v : abnormalValves) {
            SituationSnapshotVO.SituationAssetItem a = new SituationSnapshotVO.SituationAssetItem();
            a.setId(v.getId());
            a.setCode(v.getValveCode());
            a.setName(v.getValveName());
            a.setAssetType("阀门");
            a.setAbnormalType(v.getStatus());
            a.setAbnormalName(v.getStatus() == 2 ? "维修中" : v.getStatus() == 3 ? "停用" : "异常");
            a.setLng(v.getLng());
            a.setLat(v.getLat());
            a.setUpdateTime(v.getUpdateTime());
            avList.add(a);
        }
        vo.setAbnormalValves(avList);

        List<SituationSnapshotVO.SituationAreaStat> areaStats = new ArrayList<>();
        String[] areaCodes = {"AREA001", "AREA002", "AREA003", "AREA004", "AREA005"};
        String[] areaNames = {"东城区", "西城区", "南城区", "北城区", "中心区"};
        Random rnd = new Random();
        for (int i = 0; i < areaCodes.length; i++) {
            SituationSnapshotVO.SituationAreaStat s = new SituationSnapshotVO.SituationAreaStat();
            s.setAreaCode(areaCodes[i]);
            s.setAreaName(areaNames[i]);
            s.setAlarmCount(rnd.nextInt(8));
            s.setIncidentCount(rnd.nextInt(4));
            s.setWorkOrderCount(rnd.nextInt(12));
            int lv = 1;
            if (s.getAlarmCount() + s.getIncidentCount() > 8) lv = 3;
            else if (s.getAlarmCount() + s.getIncidentCount() > 4) lv = 2;
            s.setLevel(lv);
            s.setLevelName(lv == 1 ? "平稳" : lv == 2 ? "关注" : "预警");
            s.setCenterLng(new BigDecimal("116." + (30 + rnd.nextInt(30))));
            s.setCenterLat(new BigDecimal("39." + (80 + rnd.nextInt(20))));
            areaStats.add(s);
        }
        vo.setAreaStats(areaStats);

        List<SituationSnapshotVO.SituationPipelineTypeStat> pts = new ArrayList<>();
        for (PipelineTypeEnum pt : PipelineTypeEnum.values()) {
            SituationSnapshotVO.SituationPipelineTypeStat s = new SituationSnapshotVO.SituationPipelineTypeStat();
            s.setPipelineType(pt.getCode());
            s.setPipelineTypeName(pt.getLabel());
            s.setAssetCount(100 + rnd.nextInt(500));
            s.setAlarmCount(rnd.nextInt(6));
            s.setIncidentCount(rnd.nextInt(3));
            s.setHealthScore(new BigDecimal(100 - s.getAlarmCount() * 5 - rnd.nextInt(10))
                    .setScale(1, RoundingMode.HALF_UP));
            pts.add(s);
        }
        vo.setPipelineTypeStats(pts);

        SituationSnapshotVO.SituationForecast forecast = new SituationSnapshotVO.SituationForecast();
        List<String> labels = new ArrayList<>();
        List<Integer> alarms = new ArrayList<>();
        List<Integer> wos = new ArrayList<>();
        List<Integer> riskIdx = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 24; i++) {
            LocalDateTime t = now.plusHours(i);
            labels.add(t.format(DateTimeFormatter.ofPattern("HH:mm")));
            int hour = t.getHour();
            int base = 2;
            if (hour >= 7 && hour <= 9) base = 6;
            else if (hour >= 17 && hour <= 19) base = 5;
            else if (hour >= 23 || hour <= 5) base = 1;
            int ac = base + rnd.nextInt(3);
            alarms.add(ac);
            wos.add(base + rnd.nextInt(4));
            if (ac >= 5) riskIdx.add(i);
        }
        forecast.setTimeLabels(labels);
        forecast.setPredictedAlarmCounts(alarms);
        forecast.setPredictedWorkOrderCounts(wos);
        forecast.setHighRiskHourIndices(riskIdx);
        forecast.setRiskTip(riskIdx.isEmpty() ? "未来24小时整体平稳" : "早高峰和晚高峰时段注意告警预警");
        vo.setForecast(forecast);

        return vo;
    }

    @Override
    public EventReplayVO getEventReplay(Long eventId) {
        EventIncident e = incidentMapper.selectById(eventId);
        if (e == null) e = new EventIncident();

        EventReplayVO vo = new EventReplayVO();
        EventReplayVO.EventInfo info = new EventReplayVO.EventInfo();
        info.setId(e.getId());
        info.setEventCode(e.getEventCode() != null ? e.getEventCode() : "EV-DEMO-001");
        info.setTitle(e.getTitle() != null ? e.getTitle() : "示例事件：燃气主管网压力异常");
        info.setEventType(e.getEventType() != null ? e.getEventType() : 2);
        info.setEventTypeName(info.getEventType() < EVENT_TYPE_NAMES.length ? EVENT_TYPE_NAMES[info.getEventType()] : "事件");
        info.setEventLevel(e.getEventLevel() != null ? e.getEventLevel() : 3);
        info.setEventLevelName(EventLevelEnum.getLabel(info.getEventLevel()));
        info.setEventLevelColor(EventLevelEnum.getColor(info.getEventLevel()));
        info.setLng(e.getLng() != null ? e.getLng() : new BigDecimal("116.404"));
        info.setLat(e.getLat() != null ? e.getLat() : new BigDecimal("39.915"));
        info.setAffectRadius(e.getAffectRadius() != null ? e.getAffectRadius() : new BigDecimal("300"));
        info.setAreaName(e.getAreaName() != null ? e.getAreaName() : "中心区");
        info.setDescription(e.getDescription() != null ? e.getDescription() :
                "压力传感器告警值超阈值15%，疑似中压管网泄漏，已自动关阀隔离。");
        info.setDiscoverTime(e.getDiscoverTime() != null ? e.getDiscoverTime() : LocalDateTime.now().minusHours(3).format(DTF));
        info.setFirstResponseTime(LocalDateTime.now().minusHours(3).minusMinutes(7).format(DTF));
        info.setStartDisposalTime(LocalDateTime.now().minusHours(2).minusMinutes(45).format(DTF));
        info.setFinishDisposalTime(LocalDateTime.now().minusMinutes(10).format(DTF));
        info.setRecoverTime(e.getRecoverTime());
        int total = 185;
        info.setUsedMinutes(total);
        info.setUsedMinutesText("3小时5分钟");
        info.setStatus(e.getStatus() != null ? e.getStatus() : 4);
        info.setStatusName(info.getStatus() == 1 ? "待响应" : info.getStatus() == 2 ? "处置中" :
                info.getStatus() == 3 ? "已控制" : info.getStatus() == 4 ? "已处置" :
                        info.getStatus() == 5 ? "已恢复" : "已归档");
        info.setCurrentStage(e.getCurrentStage() != null ? e.getCurrentStage() : "现场恢复中");
        info.setProgress(e.getProgress() != null ? e.getProgress() : 85);
        info.setResponsiblePerson(e.getResponsiblePerson() != null ? e.getResponsiblePerson() : "张工");
        info.setCommander(e.getCommander() != null ? e.getCommander() : "李指挥");
        info.setInvolvedDeptCount(e.getInvolvedDeptCount() != null ? e.getInvolvedDeptCount() : 4);
        info.setEstimatedAffectedUsers(e.getEstimatedAffectedUsers() != null ? e.getEstimatedAffectedUsers() : 320);
        info.setActualAffectedUsers(e.getActualAffectedUsers() != null ? e.getActualAffectedUsers() : 286);
        vo.setEvent(info);

        List<EventTimelinePoint> pts = timelineMapper.selectByEventId(eventId);
        if (pts.isEmpty()) pts = generateDemoTimeline(info);
        LocalDateTime base = safeParse(info.getDiscoverTime());
        List<EventReplayVO.TimelinePoint> tl = new ArrayList<>();
        int seq = 1;
        for (EventTimelinePoint p : pts) {
            EventReplayVO.TimelinePoint tp = new EventReplayVO.TimelinePoint();
            tp.setId(p.getId() != null ? p.getId() : (long) seq);
            tp.setPointType(p.getPointType());
            tp.setPointTypeName(p.getPointType() != null && p.getPointType() < POINT_TYPE_NAMES.length
                    ? POINT_TYPE_NAMES[p.getPointType()] : "节点");
            tp.setTitle(p.getTitle());
            tp.setDescription(p.getDescription());
            tp.setOccurTime(p.getOccurTime());
            LocalDateTime ot = safeParse(p.getOccurTime());
            if (base != null && ot != null) {
                tp.setRelativeSeconds(Duration.between(base, ot).getSeconds());
            }
            tp.setDurationMinutes(p.getDurationMinutes());
            tp.setResourceType(p.getResourceType());
            tp.setResourceTypeName(ResourceTypeEnum.getLabel(p.getResourceType()));
            tp.setResourceId(p.getResourceId());
            tp.setResourceCode(p.getResourceCode());
            tp.setLng(p.getLng());
            tp.setLat(p.getLat());
            tp.setOperatorName(p.getOperatorName());
            tp.setTags(p.getTags());
            tp.setImportance(p.getImportance());
            tp.setImportanceName(p.getImportance() == null ? "一般" :
                    p.getImportance() >= 3 ? "重要" : p.getImportance() >= 2 ? "一般" : "轻微");
            tp.setStatusText("正常");
            tl.add(tp);
            seq++;
        }
        vo.setTimeline(tl);

        vo.setAssetSnapshots(generateAssetSnapshots(info, tl));

        EventReplayVO.EventSummary s = new EventReplayVO.EventSummary();
        s.setTotalResponseMinutes(7);
        s.setTotalDisposalMinutes(163);
        s.setTotalRecoverMinutes(15);
        s.setGoodPoints("响应及时，7分钟内到达现场；第一时间关阀隔离控制影响范围；多部门协同顺畅。");
        s.setImprovementPoints("老旧管网基础数据需补全，现场抢修工具需优化；用户告知方式可提升。");
        s.setInvolvedPersonCount(18);
        s.setInvolvedDeptCount(info.getInvolvedDeptCount());
        s.setNotificationsCount(12);
        s.setCostEstimate(new BigDecimal("86500.00"));
        vo.setSummary(s);

        return vo;
    }

    @Override
    public PageResult<EventIncident> selectIncidentPage(PageQuery query, Integer eventType, Integer eventLevel,
                                                         Integer status, String areaCode) {
        if (query == null) query = new PageQuery();
        query.setPageNum(query.getPageNum() != null ? query.getPageNum() : 1);
        query.setPageSize(query.getPageSize() != null ? query.getPageSize() : 10);
        LambdaQueryWrapper<EventIncident> w = new LambdaQueryWrapper<>();
        if (eventType != null) w.eq(EventIncident::getEventType, eventType);
        if (eventLevel != null) w.eq(EventIncident::getEventLevel, eventLevel);
        if (status != null) w.eq(EventIncident::getStatus, status);
        if (StrUtil.isNotBlank(areaCode)) w.eq(EventIncident::getAreaCode, areaCode);
        w.orderByDesc(EventIncident::getDiscoverTime);
        Page<EventIncident> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(incidentMapper.selectPage(page, w));
    }

    private <T> List<SituationSnapshotVO.SituationEventItem> toEventItems(List<T> list, java.util.function.Function<T, SituationSnapshotVO.SituationEventItem> fn) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(fn).collect(Collectors.toList());
    }

    private String calcDurationText(String timeStr) {
        if (StrUtil.isBlank(timeStr)) return "";
        try {
            LocalDateTime t = LocalDateTime.parse(timeStr, DTF);
            long m = Duration.between(t, LocalDateTime.now()).toMinutes();
            if (m < 60) return m + "分钟前";
            long h = m / 60;
            if (h < 24) return h + "小时前";
            return (h / 24) + "天前";
        } catch (Exception e) {
            return "";
        }
    }

    private LocalDateTime safeParse(String s) {
        if (StrUtil.isBlank(s)) return null;
        try {
            return LocalDateTime.parse(s, DTF);
        } catch (Exception e) {
            return null;
        }
    }

    private List<EventTimelinePoint> generateDemoTimeline(EventReplayVO.EventInfo info) {
        List<EventTimelinePoint> list = new ArrayList<>();
        LocalDateTime base = safeParse(info.getDiscoverTime());
        if (base == null) base = LocalDateTime.now().minusHours(3);

        list.add(makePoint(1, base, "传感器告警", "S003号压力传感器读数超阈值15%，自动触发告警",
                4, null, null, null, info.getLng(), info.getLat(), "系统", "自动告警", 4));
        list.add(makePoint(2, base.plusMinutes(2), "派单通知", "系统自动派单至燃气抢修一班，短信+系统消息双渠道通知",
                0, 14, 5L, "WO-20260614-0089", info.getLng(), info.getLat(), "调度中心", "派单", 3));
        list.add(makePoint(4, base.plusMinutes(7), "现场到达", "抢修组到达现场，开始初步排查",
                2, null, null, null, info.getLng(), info.getLat(), "王班长", "到达", 3));
        list.add(makePoint(5, base.plusMinutes(15), "关阀作业", "根据建议方案，依次关闭V-128、V-129两道阀进行隔离",
                35, 2, 1023L, "V-128", info.getLng(), info.getLat(), "李师傅", "关阀", 4));
        list.add(makePoint(8, base.plusMinutes(55), "抢修作业", "定位漏点，开挖作业，DN300焊缝修补",
                105, null, null, null, info.getLng(), info.getLat(), "抢修组", "开挖+抢修", 4));
        list.add(makePoint(13, base.plusMinutes(162), "关键节点", "压力检测合格，申请验收",
                0, 9, 9L, "WO-20260614-0089", info.getLng(), info.getLat(), "王班长", "验收申请", 3));
        list.add(makePoint(9, base.plusMinutes(165), "处置完成", "修补完成，缓慢开启阀门恢复供气",
                15, null, null, null, info.getLng(), info.getLat(), "李师傅", "恢复", 4));
        list.add(makePoint(10, base.plusMinutes(180), "验收通过", "验收合格，事件闭环",
                0, 9, 9L, "WO-20260614-0089", info.getLng(), info.getLat(), "张工", "验收", 4));
        list.add(makePoint(12, base.plusMinutes(185), "事件结束", "用户全面恢复，事件归档",
                0, null, null, null, info.getLng(), info.getLat(), "系统", "归档", 3));
        return list;
    }

    private EventTimelinePoint makePoint(int type, LocalDateTime time, String title, String desc,
                                         Integer duration, Integer resType, Long resId, String resCode,
                                         BigDecimal lng, BigDecimal lat, String op, String tags, Integer imp) {
        EventTimelinePoint p = new EventTimelinePoint();
        p.setPointType(type);
        p.setTitle(title);
        p.setDescription(desc);
        p.setOccurTime(time.format(DTF));
        p.setDurationMinutes(duration);
        p.setResourceType(resType);
        p.setResourceId(resId);
        p.setResourceCode(resCode);
        p.setLng(lng);
        p.setLat(lat);
        p.setOperatorName(op);
        p.setTags(tags);
        p.setImportance(imp);
        return p;
    }

    private List<EventReplayVO.AssetSnapshot> generateAssetSnapshots(EventReplayVO.EventInfo info,
                                                                     List<EventReplayVO.TimelinePoint> tl) {
        List<EventReplayVO.AssetSnapshot> snapshots = new ArrayList<>();
        if (tl.isEmpty()) return snapshots;
        int[] pickIdx = {0, 2, 3, 5, 7};
        for (int i : pickIdx) {
            if (i >= tl.size()) continue;
            EventReplayVO.TimelinePoint p = tl.get(i);
            EventReplayVO.AssetSnapshot s = new EventReplayVO.AssetSnapshot();
            s.setAtTime(p.getOccurTime());

            List<EventReplayVO.AssetStatusItem> vs = new ArrayList<>();
            EventReplayVO.AssetStatusItem v1 = new EventReplayVO.AssetStatusItem();
            v1.setId(1023L);
            v1.setCode("V-128");
            v1.setName("人民路中压阀128");
            v1.setStatus(p.getPointType() != null && p.getPointType() >= 5 ? 2 : 1);
            v1.setStatusName(v1.getStatus() == 2 ? "已关闭" : "正常开启");
            v1.setLng(info.getLng());
            v1.setLat(info.getLat());
            vs.add(v1);
            EventReplayVO.AssetStatusItem v2 = new EventReplayVO.AssetStatusItem();
            v2.setId(1024L);
            v2.setCode("V-129");
            v2.setName("人民路中压阀129");
            v2.setStatus(p.getPointType() != null && p.getPointType() >= 5 ? 2 : 1);
            v2.setStatusName(v2.getStatus() == 2 ? "已关闭" : "正常开启");
            v2.setLng(info.getLng());
            v2.setLat(info.getLat());
            vs.add(v2);
            s.setValves(vs);

            List<EventReplayVO.AssetStatusItem> as = new ArrayList<>();
            EventReplayVO.AssetStatusItem a1 = new EventReplayVO.AssetStatusItem();
            a1.setId(2001L);
            a1.setCode("AL-20260614-0112");
            a1.setName(info.getTitle());
            a1.setStatus(p.getPointType() != null && p.getPointType() >= 8 ? 3 : 1);
            a1.setStatusName(a1.getStatus() == 3 ? "已处置" : "处置中");
            a1.setLng(info.getLng());
            a1.setLat(info.getLat());
            as.add(a1);
            s.setAlarms(as);

            List<EventReplayVO.AssetStatusItem> ws = new ArrayList<>();
            EventReplayVO.AssetStatusItem w1 = new EventReplayVO.AssetStatusItem();
            w1.setId(5001L);
            w1.setCode("WO-20260614-0089");
            w1.setName(info.getTitle() + "抢修工单");
            int wsValue = 1;
            if (p.getPointType() != null) {
                if (p.getPointType() >= 10) wsValue = 5;
                else if (p.getPointType() >= 8) wsValue = 4;
                else if (p.getPointType() >= 5) wsValue = 3;
                else if (p.getPointType() >= 2) wsValue = 2;
            }
            w1.setStatus(wsValue);
            w1.setStatusName(WorkOrderStatusEnum.getLabel(wsValue));
            w1.setLng(info.getLng());
            w1.setLat(info.getLat());
            ws.add(w1);
            s.setWorkOrders(ws);

            s.setExcavations(Collections.emptyList());
            snapshots.add(s);
        }
        return snapshots;
    }
}
