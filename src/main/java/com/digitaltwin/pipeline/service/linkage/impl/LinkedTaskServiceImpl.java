package com.digitaltwin.pipeline.service.linkage.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.linkage.LinkedTaskDetailVO;
import com.digitaltwin.pipeline.dto.linkage.LinkedTaskTriggerDTO;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.auth.SysDepartment;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.linkage.LinkedTask;
import com.digitaltwin.pipeline.entity.linkage.LinkedTaskFlow;
import com.digitaltwin.pipeline.entity.linkage.LinkageNotification;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.enums.*;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.auth.SysDepartmentMapper;
import com.digitaltwin.pipeline.mapper.construction.ExcavationApplicationMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.mapper.linkage.LinkedTaskFlowMapper;
import com.digitaltwin.pipeline.mapper.linkage.LinkedTaskMapper;
import com.digitaltwin.pipeline.mapper.linkage.LinkageNotificationMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.service.linkage.LinkedTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedTaskServiceImpl implements LinkedTaskService {

    private final LinkedTaskMapper taskMapper;
    private final LinkedTaskFlowMapper flowMapper;
    private final LinkageNotificationMapper notificationMapper;
    private final AlarmMapper alarmMapper;
    private final HazardMapper hazardMapper;
    private final ExcavationApplicationMapper excavationMapper;
    private final WorkOrderMapper workOrderMapper;
    private final PipelineMapper pipelineMapper;
    private final SysDepartmentMapper departmentMapper;

    private static final String[] TASK_TYPE_NAMES = {"", "开挖高风险巡检", "告警触发抢修",
            "隐患整改", "审批联动待办", "跨部门协同", "综合指挥任务"};
    private static final String[] NOTIFY_TYPE_NAMES = {"", "短信", "系统消息", "邮件", "钉钉", "企业微信", "电话"};
    private static final String[] NOTIFY_STATUS_NAMES = {"待发送", "已发送", "发送失败", "已读", "已确认"};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkedTask triggerFromSource(LinkedTaskTriggerDTO dto) {
        if (dto == null || dto.getSourceType() == null || dto.getSourceId() == null) {
            throw new BusinessException("触发来源不能为空");
        }

        SourceContext ctx = loadSourceContext(dto);

        LinkedTask task = new LinkedTask();
        task.setTaskCode(generateTaskCode());
        task.setTitle(StrUtil.isNotBlank(dto.getTitle()) ? dto.getTitle() : ctx.autoTitle);
        task.setTaskType(dto.getTaskType() != null ? dto.getTaskType() : ctx.defaultTaskType);
        task.setSourceType(dto.getSourceType());
        task.setSourceId(dto.getSourceId());
        task.setSourceCode(ctx.sourceCode);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : ctx.autoPriority);
        task.setEventLevel(mapPriorityToEvent(task.getPriority()));
        task.setLng(dto.getLng() != null ? dto.getLng() : ctx.lng);
        task.setLat(dto.getLat() != null ? dto.getLat() : ctx.lat);
        task.setAreaCode(ctx.areaCode);
        task.setAreaName(ctx.areaName);
        task.setPipelineType(ctx.pipelineType);

        resolveUndertakeInfo(task, dto, ctx);

        String desc = StrUtil.isNotBlank(dto.getDescription()) ? dto.getDescription() : ctx.autoDesc;
        task.setDescription(desc);
        task.setDisposalRequirement(ctx.disposalRequirement);

        if (StrUtil.isNotBlank(dto.getDeadline())) {
            task.setDeadline(dto.getDeadline());
        } else {
            int addHours = switch (task.getPriority()) {
                case 4 -> 2;
                case 3 -> 8;
                case 2 -> 24;
                default -> 72;
            };
            task.setDeadline(LocalDateTime.now().plusHours(addHours)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        task.setStatus(1);
        task.setCurrentNode("待派单");
        task.setProgress(0);
        task.setNotifiedDeptCount(0);
        taskMapper.insert(task);

        saveFlow(task.getId(), task.getTaskCode(), 1, "创建联动任务",
                "来源:" + ResourceTypeEnum.getLabel(task.getSourceType()) + " #" + task.getSourceCode(),
                "系统触发", null, null, 0, 1);

        if (dto.getAutoNotifyDepartments() == null || dto.getAutoNotifyDepartments() == 1) {
            autoNotifyRelatedDepartments(task, ctx);
        }

        if (dto.getAutoGenerateWorkOrder() == null || dto.getAutoGenerateWorkOrder() == 1) {
            autoGenerateChildWorkOrder(task, dto, ctx);
        }

        return task;
    }

    @Override
    public LinkedTaskDetailVO getDetail(Long taskId) {
        LinkedTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("联动任务不存在");

        LinkedTaskDetailVO vo = new LinkedTaskDetailVO();

        LinkedTaskDetailVO.TaskInfo ti = new LinkedTaskDetailVO.TaskInfo();
        ti.setId(task.getId());
        ti.setTaskCode(task.getTaskCode());
        ti.setTitle(task.getTitle());
        ti.setTaskType(task.getTaskType());
        ti.setTaskTypeName(task.getTaskType() != null && task.getTaskType() < TASK_TYPE_NAMES.length
                ? TASK_TYPE_NAMES[task.getTaskType()] : "未知");
        ti.setPriority(task.getPriority());
        ti.setPriorityName(PriorityLevelEnum.getLabel(task.getPriority()));
        ti.setPriorityColor(PriorityLevelEnum.getColor(task.getPriority()));
        ti.setEventLevel(task.getEventLevel());
        ti.setEventLevelName(EventLevelEnum.getLabel(task.getEventLevel()));
        ti.setLng(task.getLng());
        ti.setLat(task.getLat());
        ti.setAreaCode(task.getAreaCode());
        ti.setAreaName(task.getAreaName());
        ti.setPipelineType(task.getPipelineType());
        ti.setPipelineTypeName(PipelineTypeEnum.getLabel(task.getPipelineType()));
        ti.setUndertakeDeptName(task.getUndertakeDeptName());
        ti.setUndertakerName(task.getUndertakerName());
        ti.setCoDeptNames(task.getCoDeptNames());
        ti.setDescription(task.getDescription());
        ti.setDisposalRequirement(task.getDisposalRequirement());
        ti.setDeadline(task.getDeadline());
        ti.setStatus(task.getStatus());
        ti.setStatusName(WorkOrderStatusEnum.getLabel(task.getStatus()));
        ti.setCurrentNode(task.getCurrentNode());
        ti.setProgress(task.getProgress());
        ti.setActualFinishTime(task.getActualFinishTime());
        ti.setUsedMinutes(task.getUsedMinutes());
        vo.setTask(ti);

        SourceContext ctx = loadSourceContext(task.getSourceType(), task.getSourceId());
        LinkedTaskDetailVO.SourceSnapshot src = new LinkedTaskDetailVO.SourceSnapshot();
        src.setSourceType(task.getSourceType());
        src.setSourceTypeName(ResourceTypeEnum.getLabel(task.getSourceType()));
        src.setSourceId(task.getSourceId());
        src.setSourceCode(task.getSourceCode());
        src.setSourceTitle(ctx.autoTitle);
        src.setSourceStatus(ctx.sourceStatus);
        src.setSourceStatusName(ctx.sourceStatusName);
        src.setExtraSummary(ctx.autoDesc);
        vo.setSource(src);

        List<LinkedTaskFlow> flows = flowMapper.selectByTaskId(taskId);
        List<LinkedTaskDetailVO.FlowRecord> flowVOS = new ArrayList<>();
        for (LinkedTaskFlow f : flows) {
            LinkedTaskDetailVO.FlowRecord fr = new LinkedTaskDetailVO.FlowRecord();
            fr.setId(f.getId());
            fr.setOperationType(f.getOperationType());
            fr.setOperationTypeName(OperationTypeEnum.getLabel(f.getOperationType()));
            fr.setOperationNode(f.getOperationNode());
            fr.setOperationContent(f.getOperationContent());
            fr.setOperatorName(f.getOperatorName());
            fr.setOperatorDept(f.getOperatorDept());
            fr.setOperationTime(f.getOperationTime());
            fr.setBeforeStatus(f.getBeforeStatus());
            fr.setBeforeStatusName(WorkOrderStatusEnum.getLabel(f.getBeforeStatus()));
            fr.setAfterStatus(f.getAfterStatus());
            fr.setAfterStatusName(WorkOrderStatusEnum.getLabel(f.getAfterStatus()));
            fr.setRemark(f.getRemark());
            flowVOS.add(fr);
        }
        vo.setFlows(flowVOS);

        List<LinkageNotification> notifies = notificationMapper.selectByTaskId(taskId);
        List<LinkedTaskDetailVO.NotifyRecord> nv = new ArrayList<>();
        for (LinkageNotification n : notifies) {
            LinkedTaskDetailVO.NotifyRecord nr = new LinkedTaskDetailVO.NotifyRecord();
            nr.setId(n.getId());
            nr.setNotifyType(n.getNotifyType());
            nr.setNotifyTypeName(n.getNotifyType() != null && n.getNotifyType() < NOTIFY_TYPE_NAMES.length
                    ? NOTIFY_TYPE_NAMES[n.getNotifyType()] : "");
            nr.setReceiverName(n.getReceiverName());
            nr.setTitle(n.getTitle());
            nr.setContent(n.getContent());
            nr.setLevel(n.getLevel());
            nr.setLevelName(PriorityLevelEnum.getLabel(n.getLevel()));
            nr.setSendStatus(n.getSendStatus());
            nr.setSendStatusName(n.getSendStatus() != null && n.getSendStatus() < NOTIFY_STATUS_NAMES.length
                    ? NOTIFY_STATUS_NAMES[n.getSendStatus()] : "");
            nr.setSendTime(n.getSendTime());
            nr.setReadTime(n.getReadTime());
            nr.setConfirmTime(n.getConfirmTime());
            nv.add(nr);
        }
        vo.setNotifications(nv);

        List<LinkedTaskDetailVO.RelatedWorkOrder> rwos = new ArrayList<>();
        if (StrUtil.isNotBlank(task.getRelatedWorkOrderIds())) {
            String[] ids = task.getRelatedWorkOrderIds().split(",");
            for (String id : ids) {
                try {
                    WorkOrder wo = workOrderMapper.selectById(Long.parseLong(id.trim()));
                    if (wo != null) {
                        LinkedTaskDetailVO.RelatedWorkOrder r = new LinkedTaskDetailVO.RelatedWorkOrder();
                        r.setId(wo.getId());
                        r.setOrderCode(wo.getOrderCode());
                        r.setTitle(wo.getTitle());
                        r.setStatus(wo.getStatus());
                        r.setStatusName(WorkOrderStatusEnum.getLabel(wo.getStatus()));
                        r.setProgress(wo.getProgress());
                        r.setUndertaker(wo.getUndertaker());
                        r.setActualStartTime(wo.getActualStartTime());
                        rwos.add(r);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        vo.setWorkOrders(rwos);

        vo.setInspections(Collections.emptyList());

        List<LinkedTaskDetailVO.TraceNode> chain = new ArrayList<>();
        LinkedTaskDetailVO.TraceNode t1 = new LinkedTaskDetailVO.TraceNode();
        t1.setStep(1);
        t1.setNodeName("来源事件触发");
        t1.setTime(src.getSourceStatusName() != null ? src.getSourceStatusName() : "触发");
        t1.setDescription("来源:" + ResourceTypeEnum.getLabel(task.getSourceType()) + " " + task.getSourceCode());
        t1.setStatus("已触发");
        chain.add(t1);
        for (int i = 0; i < flowVOS.size(); i++) {
            LinkedTaskDetailVO.FlowRecord f = flowVOS.get(i);
            LinkedTaskDetailVO.TraceNode tn = new LinkedTaskDetailVO.TraceNode();
            tn.setStep(i + 2);
            tn.setNodeName(f.getOperationNode());
            tn.setTime(f.getOperationTime());
            tn.setOperator(f.getOperatorName());
            tn.setDescription(f.getOperationContent());
            tn.setStatus(f.getAfterStatusName());
            chain.add(tn);
        }
        vo.setTraceChain(chain);

        return vo;
    }

    @Override
    public PageResult<LinkedTask> selectPage(PageQuery query, Integer status, Integer priority,
                                             Integer taskType, Integer sourceType, String areaCode) {
        if (query == null) query = new PageQuery();
        query.setPageNum(query.getPageNum() != null ? query.getPageNum() : 1);
        query.setPageSize(query.getPageSize() != null ? query.getPageSize() : 10);
        LambdaQueryWrapper<LinkedTask> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(LinkedTask::getStatus, status);
        if (priority != null) w.eq(LinkedTask::getPriority, priority);
        if (taskType != null) w.eq(LinkedTask::getTaskType, taskType);
        if (sourceType != null) w.eq(LinkedTask::getSourceType, sourceType);
        if (StrUtil.isNotBlank(areaCode)) w.eq(LinkedTask::getAreaCode, areaCode);
        w.orderByDesc(LinkedTask::getPriority, LinkedTask::getCreateTime);
        Page<LinkedTask> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(taskMapper.selectPage(page, w));
    }

    @Override
    public List<LinkageNotification> getTaskNotifications(Long taskId) {
        return notificationMapper.selectByTaskId(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkedTask assignTask(Long taskId, Long deptId, Long userId, String operatorName) {
        LinkedTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        if (deptId != null) {
            SysDepartment dept = departmentMapper.selectById(deptId);
            if (dept != null) {
                task.setUndertakeDeptId(dept.getId());
                task.setUndertakeDeptName(dept.getDeptName());
            }
        }
        task.setStatus(2);
        task.setCurrentNode("已派单");
        task.setProgress(20);
        taskMapper.updateById(task);
        saveFlow(taskId, task.getTaskCode(), 9, "派单",
                "派单到:" + task.getUndertakeDeptName(),
                StrUtil.isNotBlank(operatorName) ? operatorName : "系统",
                task.getUndertakeDeptName(), null, 1, 2);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkedTask reportProgress(Long taskId, Integer progress, String content, String operatorName) {
        LinkedTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        task.setStatus(3);
        task.setCurrentNode("处理中");
        if (progress != null) task.setProgress(Math.min(80, Math.max(task.getProgress(), progress)));
        taskMapper.updateById(task);
        saveFlow(taskId, task.getTaskCode(), 4, "上报进度",
                StrUtil.isNotBlank(content) ? content : "进度:" + task.getProgress() + "%",
                StrUtil.isNotBlank(operatorName) ? operatorName : "现场人员",
                null, null, 2, 3);
        syncProgressToChildren(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LinkedTask completeTask(Long taskId, String result, String operatorName) {
        LinkedTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        task.setStatus(4);
        task.setCurrentNode("已完成");
        task.setProgress(100);
        task.setActualFinishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        taskMapper.updateById(task);
        saveFlow(taskId, task.getTaskCode(), 8, "任务完成",
                StrUtil.isNotBlank(result) ? result : "处置完成",
                StrUtil.isNotBlank(operatorName) ? operatorName : "承办人",
                null, null, 3, 4);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long taskId, Integer notifyType, Long receiverId, String receiverName,
                                 String title, String content, Integer level) {
        LinkedTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        LinkageNotification n = new LinkageNotification();
        n.setTaskId(taskId);
        n.setTaskCode(task.getTaskCode());
        n.setNotifyType(notifyType != null ? notifyType : 2);
        n.setReceiverType(receiverId != null ? 2 : 1);
        n.setReceiverId(receiverId);
        n.setReceiverName(receiverName);
        n.setTitle(title);
        n.setContent(content);
        n.setLevel(level != null ? level : 2);
        n.setSendStatus(1);
        n.setSendTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        n.setRetryCount(1);
        notificationMapper.insert(n);

        if (task.getNotifiedDeptCount() == null) task.setNotifiedDeptCount(0);
        task.setNotifiedDeptCount(task.getNotifiedDeptCount() + 1);
        taskMapper.updateById(task);

        saveFlow(taskId, task.getTaskCode(), 10, "发送通知",
                "通知 " + receiverName + ": " + title, "系统", null, null,
                task.getStatus(), task.getStatus());
    }

    // =========== 私有辅助方法 ===========

    private SourceContext loadSourceContext(LinkedTaskTriggerDTO dto) {
        return loadSourceContext(dto.getSourceType(), dto.getSourceId());
    }

    private SourceContext loadSourceContext(Integer sourceType, Long sourceId) {
        SourceContext ctx = new SourceContext();
        if (sourceType == null) return ctx;

        switch (sourceType) {
            case 5 -> {
                Alarm alarm = alarmMapper.selectById(sourceId);
                if (alarm != null) {
                    ctx.sourceCode = alarm.getAlarmCode();
                    ctx.autoTitle = "【告警】" + alarm.getTitle();
                    ctx.autoDesc = alarm.getDescription();
                    ctx.autoPriority = alarm.getAlarmLevel() != null ? alarm.getAlarmLevel() : 2;
                    ctx.defaultTaskType = 2;
                    ctx.lng = alarm.getLng();
                    ctx.lat = alarm.getLat();
                    ctx.areaCode = alarm.getAreaCode();
                    ctx.sourceStatus = alarm.getStatus();
                    ctx.sourceStatusName = AlarmStatusEnum.getLabel(alarm.getStatus());
                    ctx.pipelineType = alarm.getPipelineType();
                    ctx.disposalRequirement = "请按告警处置流程确认现场情况，必要时关阀隔离、抢修作业";
                    if (alarm.getPipelineId() != null) {
                        Pipeline p = pipelineMapper.selectById(alarm.getPipelineId());
                        if (p != null) { ctx.areaCode = p.getAreaCode(); ctx.areaName = p.getAreaName(); }
                    }
                }
            }
            case 6 -> {
                Hazard h = hazardMapper.selectById(sourceId);
                if (h != null) {
                    ctx.sourceCode = h.getHazardCode();
                    ctx.autoTitle = "【隐患整改】" + h.getDescription();
                    ctx.autoDesc = h.getHazardTypeDesc() + " - " + h.getDescription();
                    ctx.autoPriority = h.getRiskLevel() != null ? h.getRiskLevel() : 2;
                    ctx.defaultTaskType = 3;
                    ctx.lng = h.getLng();
                    ctx.lat = h.getLat();
                    ctx.areaCode = h.getAreaCode();
                    ctx.areaName = h.getAreaCode();
                    ctx.pipelineType = h.getPipelineType();
                    ctx.sourceStatus = h.getStatus();
                    ctx.sourceStatusName = "隐患状态:" + (h.getStatus() != null ? h.getStatus() : 0);
                    ctx.disposalRequirement = "按隐患整改要求，核实现场、制定方案、闭环消险";
                }
            }
            case 7 -> {
                ExcavationApplication ex = excavationMapper.selectById(sourceId);
                if (ex != null) {
                    ctx.sourceCode = ex.getApplicationCode();
                    ctx.autoTitle = "【开挖审批联动】" + ex.getProjectName();
                    ctx.autoDesc = StrUtil.isNotBlank(ex.getExcavationScope()) ? ex.getExcavationScope() : ex.getAddress();
                    ctx.autoPriority = 3;
                    ctx.defaultTaskType = 4;
                    ctx.lng = ex.getLng();
                    ctx.lat = ex.getLat();
                    ctx.areaCode = ex.getAreaCode();
                    ctx.pipelineType = ex.getAffectPipelineType();
                    ctx.sourceStatus = ex.getApprovalStatus();
                    ctx.sourceStatusName = ExcavationStatusEnum.getLabel(ex.getApprovalStatus());
                    ctx.disposalRequirement = "施工前现场确认管线位置、交底，施工中全程旁站监护";
                }
            }
            case 9 -> {
                WorkOrder wo = workOrderMapper.selectById(sourceId);
                if (wo != null) {
                    ctx.sourceCode = wo.getOrderCode();
                    ctx.autoTitle = "【工单协同】" + wo.getTitle();
                    ctx.autoDesc = wo.getDescription();
                    ctx.autoPriority = wo.getUrgency() != null ? wo.getUrgency() : 2;
                    ctx.defaultTaskType = 5;
                    ctx.lng = wo.getLng();
                    ctx.lat = wo.getLat();
                    ctx.areaCode = wo.getAreaCode();
                    ctx.areaName = wo.getAreaName();
                    ctx.sourceStatus = wo.getStatus();
                    ctx.sourceStatusName = WorkOrderStatusEnum.getLabel(wo.getStatus());
                    ctx.pipelineType = wo.getPipelineType();
                    ctx.disposalRequirement = "按工单要求完成作业并同步进度";
                }
            }
            default -> {
                ctx.autoTitle = "联动任务";
                ctx.autoDesc = "联动任务";
                ctx.autoPriority = 2;
                ctx.defaultTaskType = 6;
            }
        }
        return ctx;
    }

    private void resolveUndertakeInfo(LinkedTask task, LinkedTaskTriggerDTO dto, SourceContext ctx) {
        if (dto.getUndertakeDeptId() != null) {
            SysDepartment dept = departmentMapper.selectById(dto.getUndertakeDeptId());
            if (dept != null) {
                task.setUndertakeDeptId(dept.getId());
                task.setUndertakeDeptName(dept.getDeptName());
                task.setUndertakerId(dto.getUndertakerId());
            }
        } else if (ctx.pipelineType != null) {
            List<SysDepartment> all = departmentMapper.selectList(null);
            for (SysDepartment dept : all) {
                if (dept.getDeptType() != null && dept.getDeptType() == 2) {
                    task.setUndertakeDeptId(dept.getId());
                    task.setUndertakeDeptName(dept.getDeptName());
                    task.setUndertakerName(dept.getLeader());
                    break;
                }
            }
        }
        if (CollUtil.isNotEmpty(dto.getCoDeptIds())) {
            List<String> names = new ArrayList<>();
            for (Long id : dto.getCoDeptIds()) {
                SysDepartment dept = departmentMapper.selectById(id);
                if (dept != null) names.add(dept.getDeptName());
            }
            task.setCoDeptIds(dto.getCoDeptIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
            task.setCoDeptNames(String.join(",", names));
        }
    }

    private void autoNotifyRelatedDepartments(LinkedTask task, SourceContext ctx) {
        List<SysDepartment> all = departmentMapper.selectList(null);
        int count = 0;
        for (SysDepartment dept : all) {
            if (count >= 5) break;
            boolean shouldNotify = false;
            if (dept.getDeptType() != null) {
                if (dept.getDeptType() == 2) shouldNotify = true;
                if (task.getPriority() != null && task.getPriority() >= 3 && dept.getDeptType() == 4) shouldNotify = true;
                if (task.getPipelineType() != null && task.getPipelineType() == 3 && dept.getDeptType() == 5) shouldNotify = true;
            }
            if (shouldNotify) {
                sendNotification(task.getId(), 2, null, dept.getDeptName(),
                        "【联动任务】" + task.getTitle(),
                        String.format("任务编号:%s 优先级:%s 截止:%s 请尽快处理。",
                                task.getTaskCode(), PriorityLevelEnum.getLabel(task.getPriority()), task.getDeadline()),
                        task.getPriority());
                count++;
            }
        }
    }

    private void autoGenerateChildWorkOrder(LinkedTask task, LinkedTaskTriggerDTO dto, SourceContext ctx) {
        try {
            WorkOrder wo = new WorkOrder();
            wo.setOrderCode("WO-LT-" + task.getTaskCode().replace("LT-", ""));
            wo.setTitle(task.getTitle());
            wo.setOrderType(task.getTaskType() != null && task.getTaskType() == 2 ? 2 :
                    (task.getTaskType() != null && task.getTaskType() == 1 ? 3 : 1));
            wo.setOrderSource(6);
            wo.setUrgency(task.getPriority());
            wo.setDescription(task.getDescription());
            wo.setLng(task.getLng());
            wo.setLat(task.getLat());
            wo.setLocation(task.getDescription());
            wo.setAreaCode(task.getAreaCode());
            wo.setAreaName(task.getAreaName());
            wo.setPipelineType(task.getPipelineType());
            wo.setCreator("系统联动");
            wo.setCreateOrderTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            wo.setUndertakeDept(task.getUndertakeDeptName());
            wo.setUndertaker(task.getUndertakerName());
            wo.setStatus(1);
            wo.setCurrentNode("待派单(联动生成)");
            wo.setProgress(0);
            if (task.getSourceType() != null) {
                if (task.getSourceType() == 5) wo.setAlarmId(task.getSourceId());
                if (task.getSourceType() == 6) wo.setHazardId(task.getSourceId());
                if (task.getSourceType() == 7) wo.setApplicationId(task.getSourceId());
            }
            workOrderMapper.insert(wo);

            String existingIds = StrUtil.isNotBlank(task.getRelatedWorkOrderIds()) ? task.getRelatedWorkOrderIds() + "," : "";
            task.setRelatedWorkOrderIds(existingIds + wo.getId());
            taskMapper.updateById(task);
        } catch (Exception e) {
            log.warn("自动生成子工单失败: {}", e.getMessage());
        }
    }

    private void syncProgressToChildren(LinkedTask task) {
        if (StrUtil.isBlank(task.getRelatedWorkOrderIds())) return;
        String[] ids = task.getRelatedWorkOrderIds().split(",");
        for (String id : ids) {
            try {
                WorkOrder wo = workOrderMapper.selectById(Long.parseLong(id.trim()));
                if (wo != null && (wo.getStatus() == 1 || wo.getStatus() == 2)) {
                    wo.setStatus(3);
                    wo.setCurrentNode("处理中(联动同步)");
                    if (task.getProgress() != null) wo.setProgress(task.getProgress());
                    workOrderMapper.updateById(wo);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void saveFlow(Long taskId, String taskCode, Integer opType, String node, String content,
                          String opName, String opDept, String remark, Integer beforeStatus, Integer afterStatus) {
        LinkedTaskFlow f = new LinkedTaskFlow();
        f.setTaskId(taskId);
        f.setTaskCode(taskCode);
        f.setOperationType(opType);
        f.setOperationNode(node);
        f.setOperationContent(content);
        f.setOperatorName(opName);
        f.setOperatorDept(opDept);
        f.setOperationTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        f.setBeforeStatus(beforeStatus);
        f.setAfterStatus(afterStatus);
        f.setRemark(remark);
        flowMapper.insert(f);
    }

    private Integer mapPriorityToEvent(Integer priority) {
        if (priority == null) return 2;
        return switch (priority) {
            case 4 -> 4;
            case 3 -> 3;
            case 2 -> 2;
            default -> 1;
        };
    }

    private String generateTaskCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = 1000 + new java.util.Random().nextInt(9000);
        return "LT-" + date + "-" + rand;
    }

    private static class SourceContext {
        String sourceCode;
        String autoTitle;
        String autoDesc;
        Integer autoPriority = 2;
        Integer defaultTaskType = 6;
        BigDecimal lng;
        BigDecimal lat;
        String areaCode;
        String areaName;
        Integer pipelineType;
        Integer sourceStatus;
        String sourceStatusName;
        String disposalRequirement;
    }
}
