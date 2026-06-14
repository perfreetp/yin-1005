package com.digitaltwin.pipeline.service.situation.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.situation.MeetingDetailVO;
import com.digitaltwin.pipeline.entity.auth.SysDepartment;
import com.digitaltwin.pipeline.entity.auth.SysUser;
import com.digitaltwin.pipeline.entity.situation.*;
import com.digitaltwin.pipeline.enums.EventLevelEnum;
import com.digitaltwin.pipeline.mapper.auth.SysDepartmentMapper;
import com.digitaltwin.pipeline.mapper.auth.SysUserMapper;
import com.digitaltwin.pipeline.mapper.situation.*;
import com.digitaltwin.pipeline.service.situation.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingSessionMapper sessionMapper;
    private final MeetingAttendeeMapper attendeeMapper;
    private final MeetingDecisionMapper decisionMapper;
    private final MeetingSummaryMapper summaryMapper;
    private final MeetingDecisionTraceMapper decisionTraceMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String[] MEETING_TYPE_NAMES = {"", "紧急会商", "例行会商", "专题会商", "复盘会"};
    private static final String[] MEETING_STATUS_NAMES = {"", "待开始", "进行中", "已暂停", "已结束", "已归档"};
    private static final String[] MEETING_STATUS_COLORS = {"#94a3b8", "#3b82f6", "#22c55e", "#eab308", "#ef4444", "#6b7280"};
    private static final String[] ATTENDEE_TYPE_NAMES = {"", "部门", "个人", "群组"};
    private static final String[] ROLE_NAMES = {"", "主持", "参会", "列席", "记录"};
    private static final String[] JOIN_STATUS_NAMES = {"未响应", "已确认", "已加入", "已离开", "缺席"};
    private static final String[] JOIN_STATUS_COLORS = {"#94a3b8", "#3b82f6", "#22c55e", "#6b7280", "#ef4444"};
    private static final String[] DECISION_TYPE_NAMES = {"", "处置方案", "资源调度", "人员调配", "下一步行动", "其他"};
    private static final String[] DECISION_STATUS_NAMES = {"待执行", "执行中", "已完成", "已否决"};
    private static final String[] DECISION_STATUS_COLORS = {"#eab308", "#3b82f6", "#22c55e", "#94a3b8"};
    private static final String[] PRIORITY_NAMES = {"", "低", "中", "高", "紧急"};
    private static final String[] SUMMARY_STATUS_NAMES = {"", "草稿", "已审核", "已发布"};
    private static final String[] TRACE_TYPE_NAMES = {"", "进度更新", "状态变更", "备注补充", "验证确认"};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingSession createMeeting(MeetingSession session, List<Long> deptIds, String initiatorName) {
        String meetingCode = generateMeetingCode();
        session.setMeetingCode(meetingCode);
        session.setStatus(1);
        session.setDecisionCount(0);
        session.setActualAttendeeCount(0);

        if (session.getInitiatorName() == null) {
            session.setInitiatorName(initiatorName);
        }

        List<String> deptNameList = new ArrayList<>();
        int attendeeCount = 0;

        if (deptIds != null && !deptIds.isEmpty()) {
            for (Long deptId : deptIds) {
                SysDepartment dept = departmentMapper.selectById(deptId);
                if (dept != null) {
                    deptNameList.add(dept.getDeptName());

                    MeetingAttendee attendee = new MeetingAttendee();
                    attendee.setMeetingId(session.getId());
                    attendee.setMeetingCode(meetingCode);
                    attendee.setAttendeeType(1);
                    attendee.setDeptId(deptId);
                    attendee.setDeptName(dept.getDeptName());
                    attendee.setRole(2);
                    attendee.setJoinStatus(0);
                    attendee.setIsKeyDecisionMaker(0);
                    attendeeMapper.insert(attendee);

                    List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getDeptId, deptId)
                            .eq(SysUser::getStatus, 1));
                    attendeeCount += users.size();
                }
            }
            session.setDeptIds(StrUtil.join(",", deptIds));
            session.setDeptNames(StrUtil.join(",", deptNameList));
        }

        session.setAttendeeCount(attendeeCount);
        sessionMapper.insert(session);

        return session;
    }

    @Override
    public MeetingDetailVO getDetail(Long meetingId) {
        MeetingSession session = sessionMapper.selectById(meetingId);
        if (session == null) {
            throw new BusinessException("会议不存在");
        }

        MeetingDetailVO vo = new MeetingDetailVO();
        vo.setId(session.getId());
        vo.setMeetingCode(session.getMeetingCode());
        vo.setTitle(session.getTitle());
        vo.setDescription(session.getDescription());
        vo.setEventId(session.getEventId());
        vo.setEventCode(session.getEventCode());
        vo.setSourceType(session.getSourceType());
        vo.setSourceId(session.getSourceId());
        vo.setMeetingType(session.getMeetingType());
        vo.setMeetingTypeName(getMeetingTypeName(session.getMeetingType()));
        vo.setLevel(session.getLevel());
        vo.setLevelName(EventLevelEnum.getLabel(session.getLevel()));
        vo.setLevelColor(EventLevelEnum.getColor(session.getLevel()));
        vo.setStatus(session.getStatus());
        vo.setStatusName(getMeetingStatusName(session.getStatus()));
        vo.setStatusColor(getMeetingStatusColor(session.getStatus()));
        vo.setInitiatorId(session.getInitiatorId());
        vo.setInitiatorName(session.getInitiatorName());
        vo.setInitiatorDept(session.getInitiatorDept());
        vo.setStartTime(session.getStartTime());
        vo.setEndTime(session.getEndTime());
        vo.setActualStartTime(session.getActualStartTime());
        vo.setActualEndTime(session.getActualEndTime());
        vo.setDurationMinutes(session.getDurationMinutes());
        vo.setDeptIds(session.getDeptIds());
        vo.setDeptNames(session.getDeptNames());
        vo.setAttendeeCount(session.getAttendeeCount());
        vo.setActualAttendeeCount(session.getActualAttendeeCount());
        vo.setDecisionCount(session.getDecisionCount());
        vo.setRecordContent(session.getRecordContent());
        vo.setConclusion(session.getConclusion());
        vo.setFollowUpTasks(session.getFollowUpTasks());
        vo.setRemark(session.getRemark());

        List<MeetingAttendee> attendees = attendeeMapper.selectList(
                new LambdaQueryWrapper<MeetingAttendee>()
                        .eq(MeetingAttendee::getMeetingId, meetingId)
                        .orderByAsc(MeetingAttendee::getCreateTime));
        vo.setAttendees(attendees.stream().map(this::toAttendeeVO).collect(Collectors.toList()));

        List<MeetingDecision> decisions = decisionMapper.selectList(
                new LambdaQueryWrapper<MeetingDecision>()
                        .eq(MeetingDecision::getMeetingId, meetingId)
                        .orderByAsc(MeetingDecision::getDecisionNo));
        vo.setDecisions(decisions.stream().map(this::toDecisionVO).collect(Collectors.toList()));

        MeetingSummary summary = summaryMapper.selectOne(
                new LambdaQueryWrapper<MeetingSummary>()
                        .eq(MeetingSummary::getMeetingId, meetingId)
                        .last("LIMIT 1"));
        if (summary != null) {
            vo.setSummary(toSummaryVO(summary));
        }

        vo.setTimeline(generateTimeline(session, attendees, decisions));

        return vo;
    }

    @Override
    public PageResult<MeetingSession> page(PageQuery query, Integer meetingType, Integer level,
                                            Integer status, String keyword) {
        if (query == null) query = new PageQuery();
        query.setPageNum(query.getPageNum() != null ? query.getPageNum() : 1);
        query.setPageSize(query.getPageSize() != null ? query.getPageSize() : 10);

        LambdaQueryWrapper<MeetingSession> w = new LambdaQueryWrapper<>();
        if (meetingType != null) w.eq(MeetingSession::getMeetingType, meetingType);
        if (level != null) w.eq(MeetingSession::getLevel, level);
        if (status != null) w.eq(MeetingSession::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) {
            w.like(MeetingSession::getTitle, keyword)
                    .or().like(MeetingSession::getMeetingCode, keyword);
        }
        w.orderByDesc(MeetingSession::getCreateTime);

        Page<MeetingSession> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(sessionMapper.selectPage(page, w));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinMeeting(Long meetingId, Long userId, String userName, String deptName) {
        MeetingSession session = sessionMapper.selectById(meetingId);
        if (session == null) {
            throw new BusinessException("会议不存在");
        }

        if (session.getStatus() == 4 || session.getStatus() == 5) {
            throw new BusinessException("会议已结束，无法加入");
        }

        LocalDateTime now = LocalDateTime.now();

        if (session.getStatus() == 1) {
            session.setStatus(2);
            session.setActualStartTime(now);
            sessionMapper.updateById(session);
        }

        LambdaQueryWrapper<MeetingAttendee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeetingAttendee::getMeetingId, meetingId);
        if (userId != null) {
            wrapper.eq(MeetingAttendee::getUserId, userId);
        } else {
            wrapper.eq(MeetingAttendee::getUserName, userName);
        }
        MeetingAttendee attendee = attendeeMapper.selectOne(wrapper);

        if (attendee == null) {
            attendee = new MeetingAttendee();
            attendee.setMeetingId(meetingId);
            attendee.setMeetingCode(session.getMeetingCode());
            attendee.setAttendeeType(2);
            attendee.setUserId(userId);
            attendee.setUserName(userName);
            attendee.setDeptName(deptName);
            attendee.setRole(2);
            attendee.setIsKeyDecisionMaker(0);
        }

        attendee.setJoinStatus(2);
        attendee.setJoinTime(now);
        if (attendee.getResponseTime() == null) {
            attendee.setResponseTime(now);
        }

        if (attendee.getId() == null) {
            attendeeMapper.insert(attendee);
        } else {
            attendeeMapper.updateById(attendee);
        }

        Long joinedCount = attendeeMapper.selectCount(new LambdaQueryWrapper<MeetingAttendee>()
                .eq(MeetingAttendee::getMeetingId, meetingId)
                .eq(MeetingAttendee::getJoinStatus, 2));
        session.setActualAttendeeCount(joinedCount.intValue());
        sessionMapper.updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingDecision addDecision(Long meetingId, MeetingDecision decision, String creatorName) {
        MeetingSession session = sessionMapper.selectById(meetingId);
        if (session == null) {
            throw new BusinessException("会议不存在");
        }

        Integer maxNo = decisionMapper.selectObjs(new LambdaQueryWrapper<MeetingDecision>()
                        .select(MeetingDecision::getDecisionNo)
                        .eq(MeetingDecision::getMeetingId, meetingId)
                        .orderByDesc(MeetingDecision::getDecisionNo)
                        .last("LIMIT 1"))
                .stream()
                .findFirst()
                .map(o -> (Integer) o)
                .orElse(0);

        decision.setMeetingId(meetingId);
        decision.setMeetingCode(session.getMeetingCode());
        decision.setDecisionNo(maxNo + 1);
        decision.setCreatorName(creatorName);
        if (decision.getStatus() == null) {
            decision.setStatus(0);
        }
        if (decision.getPriority() == null) {
            decision.setPriority(2);
        }
        decisionMapper.insert(decision);

        session.setDecisionCount(session.getDecisionCount() + 1);
        sessionMapper.updateById(session);

        return decision;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endMeeting(Long meetingId, String conclusion, String operatorName) {
        MeetingSession session = sessionMapper.selectById(meetingId);
        if (session == null) {
            throw new BusinessException("会议不存在");
        }

        if (session.getStatus() == 4 || session.getStatus() == 5) {
            throw new BusinessException("会议已结束");
        }

        LocalDateTime now = LocalDateTime.now();
        session.setStatus(4);
        session.setActualEndTime(now);
        session.setConclusion(conclusion);

        if (session.getActualStartTime() != null) {
            long minutes = java.time.Duration.between(session.getActualStartTime(), now).toMinutes();
            session.setDurationMinutes((int) minutes);
        }

        sessionMapper.updateById(session);

        attendeeMapper.selectList(new LambdaQueryWrapper<MeetingAttendee>()
                        .eq(MeetingAttendee::getMeetingId, meetingId)
                        .eq(MeetingAttendee::getJoinStatus, 2))
                .forEach(a -> {
                    a.setJoinStatus(3);
                    a.setLeaveTime(now);
                    attendeeMapper.updateById(a);
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSummary(Long meetingId, String operatorName) {
        MeetingSession session = sessionMapper.selectById(meetingId);
        if (session == null) {
            throw new BusinessException("会议不存在");
        }

        List<MeetingAttendee> attendees = attendeeMapper.selectList(
                new LambdaQueryWrapper<MeetingAttendee>()
                        .eq(MeetingAttendee::getMeetingId, meetingId)
                        .orderByAsc(MeetingAttendee::getCreateTime));

        List<MeetingDecision> decisions = decisionMapper.selectList(
                new LambdaQueryWrapper<MeetingDecision>()
                        .eq(MeetingDecision::getMeetingId, meetingId)
                        .orderByAsc(MeetingDecision::getDecisionNo));

        MeetingSummary summary = new MeetingSummary();
        summary.setMeetingId(meetingId);
        summary.setMeetingCode(session.getMeetingCode());
        summary.setTitle(session.getTitle() + " - 会议纪要");

        StringBuilder content = new StringBuilder();
        content.append("会议名称：").append(session.getTitle()).append("\n");
        content.append("会议时间：").append(session.getActualStartTime() != null ? session.getActualStartTime() : session.getStartTime())
                .append(" 至 ").append(session.getActualEndTime() != null ? session.getActualEndTime() : session.getEndTime()).append("\n");
        content.append("会议时长：").append(session.getDurationMinutes() != null ? session.getDurationMinutes() + "分钟" : "待统计").append("\n");
        content.append("主持人：").append(session.getInitiatorName()).append("\n");
        content.append("\n--- 会议内容 ---\n");
        if (session.getRecordContent() != null) {
            content.append(session.getRecordContent()).append("\n");
        }
        content.append("\n--- 会议决议 ---\n");
        for (MeetingDecision d : decisions) {
            content.append(d.getDecisionNo()).append(". ").append(d.getTitle()).append("\n");
            if (d.getContent() != null) {
                content.append("   ").append(d.getContent()).append("\n");
            }
        }
        content.append("\n--- 会议结论 ---\n");
        if (session.getConclusion() != null) {
            content.append(session.getConclusion()).append("\n");
        }
        summary.setSummaryContent(content.toString());

        List<String> keyPoints = new ArrayList<>();
        for (MeetingDecision d : decisions) {
            keyPoints.add(d.getTitle());
        }
        summary.setKeyPoints(String.join(",", keyPoints));

        StringBuilder decisionsSb = new StringBuilder();
        for (MeetingDecision d : decisions) {
            decisionsSb.append("[").append(d.getDecisionNo()).append("] ").append(d.getTitle()).append("; ");
        }
        summary.setDecisions(decisionsSb.toString());

        StringBuilder attendeesSb = new StringBuilder();
        for (MeetingAttendee a : attendees) {
            if (a.getUserName() != null) {
                attendeesSb.append(a.getUserName());
                if (a.getDeptName() != null) {
                    attendeesSb.append("(").append(a.getDeptName()).append(")");
                }
                attendeesSb.append("、");
            }
        }
        if (attendeesSb.length() > 0) {
            attendeesSb.setLength(attendeesSb.length() - 1);
        }
        summary.setAttendeesList(attendeesSb.toString());

        StringBuilder absenteesSb = new StringBuilder();
        for (MeetingAttendee a : attendees) {
            if (a.getJoinStatus() != null && (a.getJoinStatus() == 0 || a.getJoinStatus() == 4)) {
                if (a.getUserName() != null) {
                    absenteesSb.append(a.getUserName());
                } else if (a.getDeptName() != null) {
                    absenteesSb.append(a.getDeptName());
                }
                absenteesSb.append("、");
            }
        }
        if (absenteesSb.length() > 0) {
            absenteesSb.setLength(absenteesSb.length() - 1);
        }
        summary.setAbsentees(absenteesSb.toString());

        summary.setFollowUpItems(session.getFollowUpTasks());
        summary.setGeneratedTime(LocalDateTime.now());
        summary.setGeneratorName(operatorName);
        summary.setStatus(1);
        summary.setVersion(1);

        summaryMapper.insert(summary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAttendeeStatus(Long attendeeId, Integer status, String remark) {
        MeetingAttendee attendee = attendeeMapper.selectById(attendeeId);
        if (attendee == null) {
            throw new BusinessException("参会记录不存在");
        }

        attendee.setJoinStatus(status);
        if (remark != null) {
            attendee.setRemark(remark);
        }

        LocalDateTime now = LocalDateTime.now();
        if (attendee.getResponseTime() == null && status != null && status != 0) {
            attendee.setResponseTime(now);
        }
        if (status != null && status == 2) {
            attendee.setJoinTime(now);
        }
        if (status != null && status == 3) {
            attendee.setLeaveTime(now);
        }

        attendeeMapper.updateById(attendee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingDecision updateDecision(Long decisionId, MeetingDecision decision, String operatorName) {
        MeetingDecision existing = decisionMapper.selectById(decisionId);
        if (existing == null) {
            throw new BusinessException("决议不存在");
        }

        Integer beforeStatus = existing.getStatus();
        boolean statusChanged = decision.getStatus() != null && !decision.getStatus().equals(existing.getStatus());

        if (decision.getTitle() != null) {
            existing.setTitle(decision.getTitle());
        }
        if (decision.getContent() != null) {
            existing.setContent(decision.getContent());
        }
        if (decision.getDetail() != null) {
            existing.setDetail(decision.getDetail());
        }
        if (decision.getDecisionType() != null) {
            existing.setDecisionType(decision.getDecisionType());
        }
        if (decision.getPriority() != null) {
            existing.setPriority(decision.getPriority());
        }
        if (decision.getStatus() != null) {
            existing.setStatus(decision.getStatus());
            if (decision.getStatus() == 2) {
                existing.setFinishTime(LocalDateTime.now());
            }
        }
        if (decision.getExecutorDeptId() != null) {
            existing.setExecutorDeptId(decision.getExecutorDeptId());
        }
        if (decision.getExecutorDeptName() != null) {
            existing.setExecutorDeptName(decision.getExecutorDeptName());
        }
        if (decision.getExecutorId() != null) {
            existing.setExecutorId(decision.getExecutorId());
        }
        if (decision.getExecutorName() != null) {
            existing.setExecutorName(decision.getExecutorName());
        }
        if (decision.getDeadline() != null) {
            existing.setDeadline(decision.getDeadline());
        }
        if (decision.getRemark() != null) {
            existing.setRemark(decision.getRemark());
        }

        LocalDateTime now = LocalDateTime.now();
        existing.setLastUpdateTime(now);
        existing.setLastUpdaterName(operatorName);

        decisionMapper.updateById(existing);

        MeetingDecisionTrace trace = new MeetingDecisionTrace();
        trace.setDecisionId(decisionId);
        trace.setMeetingId(existing.getMeetingId());
        trace.setTraceType(statusChanged ? 2 : 3);
        trace.setContent(statusChanged ? "决议状态变更及内容更新" : "决议内容更新");
        trace.setOperatorName(operatorName);
        trace.setOperationTime(now);
        trace.setBeforeStatus(beforeStatus);
        trace.setAfterStatus(existing.getStatus());
        trace.setBeforeProgress(existing.getCompletionPercentage());
        trace.setAfterProgress(existing.getCompletionPercentage());
        decisionTraceMapper.insert(trace);

        updateSummaryDecisionStats(existing.getMeetingId());

        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDecisionProgress(Long decisionId, Integer progress, String result, String operatorName) {
        MeetingDecision decision = decisionMapper.selectById(decisionId);
        if (decision == null) {
            throw new BusinessException("决议不存在");
        }

        Integer beforeProgress = decision.getCompletionPercentage();
        Integer beforeStatus = decision.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (progress != null) {
            if (progress < 0) progress = 0;
            if (progress > 100) progress = 100;
            decision.setCompletionPercentage(progress);

            if (progress == 100 && decision.getStatus() != 2) {
                decision.setStatus(2);
                decision.setFinishTime(now);
            } else if (progress > 0 && progress < 100 && decision.getStatus() == 0) {
                decision.setStatus(1);
            }
        }
        if (result != null) {
            decision.setActualResult(result);
        }

        if (decision.getDeadline() != null && now.isAfter(decision.getDeadline())
                && decision.getStatus() != null && decision.getStatus() != 2) {
            decision.setIsTimeout(1);
        } else {
            decision.setIsTimeout(0);
        }

        decision.setLastUpdateTime(now);
        decision.setLastUpdaterName(operatorName);

        decisionMapper.updateById(decision);

        MeetingDecisionTrace trace = new MeetingDecisionTrace();
        trace.setDecisionId(decisionId);
        trace.setMeetingId(decision.getMeetingId());
        trace.setTraceType(1);
        trace.setContent("进度更新：" + (progress != null ? progress + "%" : "无变化") + (result != null ? "，落实结果：" + result : ""));
        trace.setOperatorName(operatorName);
        trace.setOperationTime(now);
        trace.setBeforeStatus(beforeStatus);
        trace.setAfterStatus(decision.getStatus());
        trace.setBeforeProgress(beforeProgress);
        trace.setAfterProgress(decision.getCompletionPercentage());
        decisionTraceMapper.insert(trace);

        updateSummaryDecisionStats(decision.getMeetingId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyDecision(Long decisionId, String verificationRemark, String verificationPerson) {
        MeetingDecision decision = decisionMapper.selectById(decisionId);
        if (decision == null) {
            throw new BusinessException("决议不存在");
        }

        Integer beforeStatus = decision.getStatus();
        LocalDateTime now = LocalDateTime.now();

        decision.setVerificationPerson(verificationPerson);
        decision.setVerificationTime(now);
        decision.setVerificationRemark(verificationRemark);
        decision.setLastUpdateTime(now);
        decision.setLastUpdaterName(verificationPerson);

        decisionMapper.updateById(decision);

        MeetingDecisionTrace trace = new MeetingDecisionTrace();
        trace.setDecisionId(decisionId);
        trace.setMeetingId(decision.getMeetingId());
        trace.setTraceType(4);
        trace.setContent("验证确认：" + (verificationRemark != null ? verificationRemark : ""));
        trace.setOperatorName(verificationPerson);
        trace.setOperationTime(now);
        trace.setBeforeStatus(beforeStatus);
        trace.setAfterStatus(decision.getStatus());
        trace.setBeforeProgress(decision.getCompletionPercentage());
        trace.setAfterProgress(decision.getCompletionPercentage());
        decisionTraceMapper.insert(trace);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vetoDecision(Long decisionId, String remark, String operatorName) {
        MeetingDecision decision = decisionMapper.selectById(decisionId);
        if (decision == null) {
            throw new BusinessException("决议不存在");
        }

        Integer beforeStatus = decision.getStatus();
        LocalDateTime now = LocalDateTime.now();

        decision.setStatus(3);
        decision.setRemark(remark);
        decision.setLastUpdateTime(now);
        decision.setLastUpdaterName(operatorName);

        decisionMapper.updateById(decision);

        MeetingDecisionTrace trace = new MeetingDecisionTrace();
        trace.setDecisionId(decisionId);
        trace.setMeetingId(decision.getMeetingId());
        trace.setTraceType(2);
        trace.setContent("决议被否决：" + (remark != null ? remark : ""));
        trace.setOperatorName(operatorName);
        trace.setOperationTime(now);
        trace.setBeforeStatus(beforeStatus);
        trace.setAfterStatus(3);
        trace.setBeforeProgress(decision.getCompletionPercentage());
        trace.setAfterProgress(decision.getCompletionPercentage());
        decisionTraceMapper.insert(trace);

        updateSummaryDecisionStats(decision.getMeetingId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingSummary editSummary(Long meetingId, MeetingSummary summary, String editorName) {
        MeetingSummary existing = summaryMapper.selectOne(
                new LambdaQueryWrapper<MeetingSummary>()
                        .eq(MeetingSummary::getMeetingId, meetingId)
                        .last("LIMIT 1"));

        if (existing == null) {
            throw new BusinessException("会议纪要不存在，请先生成纪要");
        }

        if (summary.getTitle() != null) {
            existing.setTitle(summary.getTitle());
        }
        if (summary.getSummaryContent() != null) {
            existing.setSummaryContent(summary.getSummaryContent());
        }
        if (summary.getKeyPoints() != null) {
            existing.setKeyPoints(summary.getKeyPoints());
        }
        if (summary.getDecisions() != null) {
            existing.setDecisions(summary.getDecisions());
        }
        if (summary.getAttendeesList() != null) {
            existing.setAttendeesList(summary.getAttendeesList());
        }
        if (summary.getAbsentees() != null) {
            existing.setAbsentees(summary.getAbsentees());
        }
        if (summary.getFollowUpItems() != null) {
            existing.setFollowUpItems(summary.getFollowUpItems());
        }
        if (summary.getStatus() != null) {
            existing.setStatus(summary.getStatus());
        }
        if (summary.getApproverName() != null) {
            existing.setApproverName(summary.getApproverName());
        }
        if (summary.getAttachFileIds() != null) {
            existing.setAttachFileIds(summary.getAttachFileIds());
        }

        LocalDateTime now = LocalDateTime.now();
        existing.setEditorName(editorName);
        existing.setEditTime(now);
        existing.setVersion(existing.getVersion() + 1);

        summaryMapper.updateById(existing);

        updateSummaryDecisionStats(meetingId);

        return existing;
    }

    @Override
    public List<MeetingDecisionTrace> getDecisionTraces(Long decisionId) {
        return decisionTraceMapper.selectList(
                new LambdaQueryWrapper<MeetingDecisionTrace>()
                        .eq(MeetingDecisionTrace::getDecisionId, decisionId)
                        .orderByDesc(MeetingDecisionTrace::getOperationTime));
    }

    @Override
    public MeetingDetailVO getDecisionTrackingView(Long meetingId) {
        MeetingDetailVO detail = getDetail(meetingId);

        if (detail.getDecisions() != null) {
            for (MeetingDetailVO.DecisionVO decision : detail.getDecisions()) {
                List<MeetingDecisionTrace> traces = getDecisionTraces(decision.getId());
                decision.setTraces(traces.stream().map(this::toDecisionTraceVO).collect(Collectors.toList()));
            }
        }

        return detail;
    }

    private String generateMeetingCode() {
        String dateStr = LocalDateTime.now().format(DTF);
        String prefix = "MS-" + dateStr + "-";
        Random random = new Random();
        int num = 1000 + random.nextInt(9000);
        return prefix + num;
    }

    private String getMeetingTypeName(Integer type) {
        if (type == null || type < 1 || type >= MEETING_TYPE_NAMES.length) return "未知";
        return MEETING_TYPE_NAMES[type];
    }

    private String getMeetingStatusName(Integer status) {
        if (status == null || status < 1 || status >= MEETING_STATUS_NAMES.length) return "未知";
        return MEETING_STATUS_NAMES[status];
    }

    private String getMeetingStatusColor(Integer status) {
        if (status == null || status < 1 || status >= MEETING_STATUS_COLORS.length) return "#94a3b8";
        return MEETING_STATUS_COLORS[status];
    }

    private MeetingDetailVO.AttendeeVO toAttendeeVO(MeetingAttendee a) {
        MeetingDetailVO.AttendeeVO vo = new MeetingDetailVO.AttendeeVO();
        vo.setId(a.getId());
        vo.setMeetingId(a.getMeetingId());
        vo.setAttendeeType(a.getAttendeeType());
        vo.setAttendeeTypeName(a.getAttendeeType() != null && a.getAttendeeType() < ATTENDEE_TYPE_NAMES.length
                ? ATTENDEE_TYPE_NAMES[a.getAttendeeType()] : "未知");
        vo.setDeptId(a.getDeptId());
        vo.setDeptName(a.getDeptName());
        vo.setUserId(a.getUserId());
        vo.setUserName(a.getUserName());
        vo.setUserPhone(a.getUserPhone());
        vo.setRole(a.getRole());
        vo.setRoleName(a.getRole() != null && a.getRole() < ROLE_NAMES.length ? ROLE_NAMES[a.getRole()] : "未知");
        vo.setJoinStatus(a.getJoinStatus());
        vo.setJoinStatusName(a.getJoinStatus() != null && a.getJoinStatus() < JOIN_STATUS_NAMES.length
                ? JOIN_STATUS_NAMES[a.getJoinStatus()] : "未知");
        vo.setJoinStatusColor(a.getJoinStatus() != null && a.getJoinStatus() < JOIN_STATUS_COLORS.length
                ? JOIN_STATUS_COLORS[a.getJoinStatus()] : "#94a3b8");
        vo.setJoinTime(a.getJoinTime());
        vo.setLeaveTime(a.getLeaveTime());
        vo.setResponseTime(a.getResponseTime());
        vo.setIsKeyDecisionMaker(a.getIsKeyDecisionMaker());
        vo.setRemark(a.getRemark());
        return vo;
    }

    private MeetingDetailVO.DecisionVO toDecisionVO(MeetingDecision d) {
        MeetingDetailVO.DecisionVO vo = new MeetingDetailVO.DecisionVO();
        vo.setId(d.getId());
        vo.setMeetingId(d.getMeetingId());
        vo.setDecisionNo(d.getDecisionNo());
        vo.setTitle(d.getTitle());
        vo.setContent(d.getContent());
        vo.setDetail(d.getDetail());
        vo.setDecisionType(d.getDecisionType());
        vo.setDecisionTypeName(d.getDecisionType() != null && d.getDecisionType() < DECISION_TYPE_NAMES.length
                ? DECISION_TYPE_NAMES[d.getDecisionType()] : "未知");
        vo.setPriority(d.getPriority());
        vo.setPriorityName(d.getPriority() != null && d.getPriority() < PRIORITY_NAMES.length
                ? PRIORITY_NAMES[d.getPriority()] : "未知");
        vo.setPriorityColor(EventLevelEnum.getColor(d.getPriority()));
        vo.setStatus(d.getStatus());
        vo.setStatusName(d.getStatus() != null && d.getStatus() < DECISION_STATUS_NAMES.length
                ? DECISION_STATUS_NAMES[d.getStatus()] : "未知");
        vo.setStatusColor(d.getStatus() != null && d.getStatus() < DECISION_STATUS_COLORS.length
                ? DECISION_STATUS_COLORS[d.getStatus()] : "#94a3b8");
        vo.setExecutorDeptId(d.getExecutorDeptId());
        vo.setExecutorDeptName(d.getExecutorDeptName());
        vo.setExecutorId(d.getExecutorId());
        vo.setExecutorName(d.getExecutorName());
        vo.setDeadline(d.getDeadline());
        vo.setFinishTime(d.getFinishTime());
        vo.setRelatedWorkOrderId(d.getRelatedWorkOrderId());
        vo.setRelatedTaskId(d.getRelatedTaskId());
        vo.setCreatorName(d.getCreatorName());
        vo.setRemark(d.getRemark());
        vo.setActualResult(d.getActualResult());
        vo.setCompletionPercentage(d.getCompletionPercentage());
        vo.setIsTimeout(d.getIsTimeout());
        vo.setLastUpdateTime(d.getLastUpdateTime());
        vo.setLastUpdaterName(d.getLastUpdaterName());
        vo.setVerificationPerson(d.getVerificationPerson());
        vo.setVerificationTime(d.getVerificationTime());
        vo.setVerificationRemark(d.getVerificationRemark());
        return vo;
    }

    private MeetingDetailVO.SummaryVO toSummaryVO(MeetingSummary s) {
        MeetingDetailVO.SummaryVO vo = new MeetingDetailVO.SummaryVO();
        vo.setId(s.getId());
        vo.setMeetingId(s.getMeetingId());
        vo.setMeetingCode(s.getMeetingCode());
        vo.setTitle(s.getTitle());
        vo.setSummaryContent(s.getSummaryContent());
        vo.setKeyPoints(s.getKeyPoints());
        vo.setDecisions(s.getDecisions());
        vo.setAttendeesList(s.getAttendeesList());
        vo.setAbsentees(s.getAbsentees());
        vo.setFollowUpItems(s.getFollowUpItems());
        vo.setGeneratedTime(s.getGeneratedTime());
        vo.setGeneratorName(s.getGeneratorName());
        vo.setApproverName(s.getApproverName());
        vo.setStatus(s.getStatus());
        vo.setStatusName(s.getStatus() != null && s.getStatus() < SUMMARY_STATUS_NAMES.length
                ? SUMMARY_STATUS_NAMES[s.getStatus()] : "未知");
        vo.setVersion(s.getVersion());
        vo.setEditorName(s.getEditorName());
        vo.setEditTime(s.getEditTime());
        vo.setDecisionTotalCount(s.getDecisionTotalCount());
        vo.setDecisionCompletedCount(s.getDecisionCompletedCount());
        vo.setDecisionInProgressCount(s.getDecisionInProgressCount());
        vo.setDecisionPendingCount(s.getDecisionPendingCount());
        vo.setDecisionVetoedCount(s.getDecisionVetoedCount());
        vo.setOverallCompletionRate(s.getOverallCompletionRate());
        vo.setAttachFileIds(s.getAttachFileIds());
        return vo;
    }

    private List<MeetingDetailVO.MeetingTimelineItem> generateTimeline(MeetingSession session,
                                                                        List<MeetingAttendee> attendees,
                                                                        List<MeetingDecision> decisions) {
        List<MeetingDetailVO.MeetingTimelineItem> timeline = new ArrayList<>();

        if (session.getStartTime() != null) {
            MeetingDetailVO.MeetingTimelineItem start = new MeetingDetailVO.MeetingTimelineItem();
            start.setTime(session.getStartTime());
            start.setType(1);
            start.setTypeName("会议开始");
            start.setTitle("会议开始");
            start.setDescription(session.getTitle());
            start.setOperatorName(session.getInitiatorName());
            start.setImportance(4);
            timeline.add(start);
        }

        for (MeetingAttendee a : attendees) {
            if (a.getJoinTime() != null && a.getJoinStatus() != null && a.getJoinStatus() >= 2) {
                MeetingDetailVO.MeetingTimelineItem item = new MeetingDetailVO.MeetingTimelineItem();
                item.setTime(a.getJoinTime());
                item.setType(2);
                item.setTypeName("加入会议");
                item.setTitle((a.getUserName() != null ? a.getUserName() : a.getDeptName()) + " 加入会议");
                item.setDescription(a.getRole() != null && a.getRole() < ROLE_NAMES.length
                        ? "角色：" + ROLE_NAMES[a.getRole()] : "");
                item.setOperatorName(a.getUserName());
                item.setImportance(2);
                timeline.add(item);
            }
        }

        for (MeetingDecision d : decisions) {
            if (d.getCreateTime() != null) {
                MeetingDetailVO.MeetingTimelineItem item = new MeetingDetailVO.MeetingTimelineItem();
                item.setTime(d.getCreateTime());
                item.setType(4);
                item.setTypeName("形成决议");
                item.setTitle("第" + d.getDecisionNo() + "项决议：" + d.getTitle());
                item.setDescription(d.getContent());
                item.setOperatorName(d.getCreatorName());
                item.setImportance(3);
                timeline.add(item);
            }
        }

        if (session.getActualEndTime() != null || session.getEndTime() != null) {
            MeetingDetailVO.MeetingTimelineItem end = new MeetingDetailVO.MeetingTimelineItem();
            end.setTime(session.getActualEndTime() != null ? session.getActualEndTime() : session.getEndTime());
            end.setType(5);
            end.setTypeName("会议结束");
            end.setTitle("会议结束");
            end.setDescription(session.getConclusion());
            end.setOperatorName(session.getInitiatorName());
            end.setImportance(4);
            timeline.add(end);
        }

        timeline.sort(Comparator.comparing(MeetingDetailVO.MeetingTimelineItem::getTime));
        return timeline;
    }

    private MeetingDetailVO.DecisionTraceVO toDecisionTraceVO(MeetingDecisionTrace t) {
        MeetingDetailVO.DecisionTraceVO vo = new MeetingDetailVO.DecisionTraceVO();
        vo.setId(t.getId());
        vo.setDecisionId(t.getDecisionId());
        vo.setMeetingId(t.getMeetingId());
        vo.setTraceType(t.getTraceType());
        vo.setTraceTypeName(t.getTraceType() != null && t.getTraceType() < TRACE_TYPE_NAMES.length
                ? TRACE_TYPE_NAMES[t.getTraceType()] : "未知");
        vo.setContent(t.getContent());
        vo.setOperatorName(t.getOperatorName());
        vo.setOperationTime(t.getOperationTime());
        vo.setBeforeStatus(t.getBeforeStatus());
        vo.setBeforeStatusName(t.getBeforeStatus() != null && t.getBeforeStatus() < DECISION_STATUS_NAMES.length
                ? DECISION_STATUS_NAMES[t.getBeforeStatus()] : "");
        vo.setAfterStatus(t.getAfterStatus());
        vo.setAfterStatusName(t.getAfterStatus() != null && t.getAfterStatus() < DECISION_STATUS_NAMES.length
                ? DECISION_STATUS_NAMES[t.getAfterStatus()] : "");
        vo.setBeforeProgress(t.getBeforeProgress());
        vo.setAfterProgress(t.getAfterProgress());
        return vo;
    }

    private void updateSummaryDecisionStats(Long meetingId) {
        MeetingSummary summary = summaryMapper.selectOne(
                new LambdaQueryWrapper<MeetingSummary>()
                        .eq(MeetingSummary::getMeetingId, meetingId)
                        .last("LIMIT 1"));
        if (summary == null) {
            return;
        }

        List<MeetingDecision> decisions = decisionMapper.selectList(
                new LambdaQueryWrapper<MeetingDecision>()
                        .eq(MeetingDecision::getMeetingId, meetingId));

        int totalCount = decisions.size();
        int completedCount = 0;
        int inProgressCount = 0;
        int pendingCount = 0;
        int vetoedCount = 0;
        int totalProgress = 0;

        for (MeetingDecision d : decisions) {
            Integer status = d.getStatus();
            if (status == null) {
                pendingCount++;
            } else {
                switch (status) {
                    case 0:
                        pendingCount++;
                        break;
                    case 1:
                        inProgressCount++;
                        break;
                    case 2:
                        completedCount++;
                        break;
                    case 3:
                        vetoedCount++;
                        break;
                    default:
                        pendingCount++;
                        break;
                }
            }
            if (d.getCompletionPercentage() != null) {
                totalProgress += d.getCompletionPercentage();
            }
        }

        int overallRate = totalCount > 0 ? totalProgress / totalCount : 0;

        summary.setDecisionTotalCount(totalCount);
        summary.setDecisionCompletedCount(completedCount);
        summary.setDecisionInProgressCount(inProgressCount);
        summary.setDecisionPendingCount(pendingCount);
        summary.setDecisionVetoedCount(vetoedCount);
        summary.setOverallCompletionRate(overallRate);

        summaryMapper.updateById(summary);
    }
}
