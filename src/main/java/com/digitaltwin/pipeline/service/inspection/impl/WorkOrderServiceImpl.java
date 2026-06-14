package com.digitaltwin.pipeline.service.inspection.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderFlowDTO;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.DefectReport;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.inspection.WorkOrderFlow;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.mapper.inspection.DefectReportMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderFlowMapper;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.service.inspection.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder>
        implements WorkOrderService {

    private final WorkOrderFlowMapper flowMapper;
    private final DefectReportMapper defectMapper;
    private final HazardMapper hazardMapper;
    private final AlarmMapper alarmMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<WorkOrder> selectPage(WorkOrderQueryDTO query) {
        Page<WorkOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getOrderType(),
                query.getOrderSource(), query.getUrgency(), query.getAreaCode(),
                query.getStatus(), query.getUndertaker(), query.getKeyword()));
    }

    @Override
    public WorkOrder selectById(Long id) {
        WorkOrder order = super.getById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.WORKORDER_NOT_FOUND);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder createFromDefect(Long defectId) {
        DefectReport defect = defectMapper.selectById(defectId);
        if (defect == null) {
            throw new BusinessException("缺陷记录不存在");
        }

        WorkOrder order = new WorkOrder();
        order.setOrderCode("WO" + System.currentTimeMillis());
        order.setTitle("维修工单-" + defect.getTitle());
        order.setOrderType(1);
        order.setOrderSource(3);
        order.setUrgency(defect.getDefectLevel());
        order.setDescription(defect.getDescription());
        order.setLng(defect.getLng());
        order.setLat(defect.getLat());
        order.setLocation(defect.getLocation());
        order.setPipelineId(defect.getPipelineId());
        order.setPipelineCode(defect.getPipelineCode());
        order.setManholeId(defect.getManholeId());
        order.setValveId(defect.getValveId());
        order.setAreaCode(defect.getAreaCode());
        order.setAreaName(defect.getAreaName());
        order.setDefectId(defectId);
        order.setCreator(defect.getReporterName());
        order.setCreateOrderTime(LocalDateTime.now().format(DTF));
        order.setStatus(1);
        order.setCurrentNode("待派单");
        order.setProgress(0);
        this.save(order);

        defect.setStatus(3);
        defect.setWorkOrderId(order.getId());
        defectMapper.updateById(defect);

        addFlow(order.getId(), order.getOrderCode(), 1, null, 1,
                defect.getReporterName(), null, "系统自动生成工单", null, null, null);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder createFromHazard(Long hazardId) {
        Hazard hazard = hazardMapper.selectById(hazardId);
        if (hazard == null) {
            throw new BusinessException(ResultCode.HAZARD_NOT_FOUND);
        }

        WorkOrder order = new WorkOrder();
        order.setOrderCode("WO" + System.currentTimeMillis());
        order.setTitle("隐患整改工单-" + hazard.getTitle());
        order.setOrderType(1);
        order.setOrderSource(2);
        order.setUrgency(hazard.getUrgency() != null ? hazard.getUrgency() :
                (hazard.getRiskLevel() != null ? hazard.getRiskLevel() : 1));
        order.setDescription(hazard.getDescription());
        order.setLng(hazard.getLng());
        order.setLat(hazard.getLat());
        order.setLocation(hazard.getLocation());
        order.setPipelineId(hazard.getPipelineId());
        order.setPipelineCode(hazard.getPipelineCode());
        order.setAreaCode(hazard.getAreaCode());
        order.setAreaName(hazard.getAreaName());
        order.setHazardId(hazardId);
        order.setCreator(hazard.getDiscoverer());
        order.setCreateOrderTime(LocalDateTime.now().format(DTF));
        order.setStatus(1);
        order.setCurrentNode("待派单");
        order.setProgress(0);
        this.save(order);

        hazard.setStatus(2);
        hazard.setWorkOrderId(order.getId());
        hazardMapper.updateById(hazard);

        addFlow(order.getId(), order.getOrderCode(), 1, null, 1,
                hazard.getDiscoverer(), null, "系统自动生成隐患整改工单", null, null, null);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder createFromAlarm(Long alarmId) {
        Alarm alarm = alarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }

        WorkOrder order = new WorkOrder();
        order.setOrderCode("WO" + System.currentTimeMillis());
        order.setTitle("告警抢修工单-" + alarm.getTitle());
        order.setOrderType(2);
        order.setOrderSource(1);
        order.setUrgency(alarm.getAlarmLevel());
        order.setDescription(alarm.getContent());
        order.setLng(alarm.getLng());
        order.setLat(alarm.getLat());
        order.setLocation(alarm.getLocation());
        order.setPipelineId(alarm.getPipelineId());
        order.setPipelineCode(alarm.getPipelineCode());
        order.setAreaCode(alarm.getAreaCode());
        order.setAreaName(alarm.getAreaName());
        order.setAlarmId(alarmId);
        order.setCreator("system");
        order.setCreateOrderTime(LocalDateTime.now().format(DTF));
        order.setStatus(1);
        order.setCurrentNode("待派单");
        order.setProgress(0);
        this.save(order);

        alarm.setStatus(2);
        alarm.setWorkOrderId(order.getId());
        alarmMapper.updateById(alarm);

        addFlow(order.getId(), order.getOrderCode(), 1, null, 1,
                "system", null, "告警自动触发抢修工单", null, null, null);

        return order;
    }

    @Override
    public List<WorkOrderFlow> getFlows(Long workOrderId) {
        return flowMapper.selectByWorkOrderId(workOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder dispatch(WorkOrderFlowDTO dto) {
        WorkOrder order = selectById(dto.getWorkOrderId());
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.STATUS_ERROR);
        }

        String now = LocalDateTime.now().format(DTF);
        order.setStatus(2);
        order.setDispatchTime(now);
        order.setDispatcher(dto.getOperator());
        order.setUndertakeDept(dto.getUndertakeDept());
        order.setUndertaker(dto.getUndertaker());
        order.setContactPhone(dto.getContactPhone());
        order.setExpectCompleteTime(dto.getExpectCompleteTime());
        order.setCurrentNode("已派单");
        order.setProgress(15);
        this.updateById(order);

        addFlow(order.getId(), order.getOrderCode(), 2, 1, 2,
                dto.getOperator(), dto.getOperatorDept(), dto.getOpinion(), dto.getAttachments(),
                dto.getUndertaker(), dto.getUndertakeDept());

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder startProcess(WorkOrderFlowDTO dto) {
        WorkOrder order = selectById(dto.getWorkOrderId());
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.STATUS_ERROR);
        }

        String now = LocalDateTime.now().format(DTF);
        order.setStatus(3);
        order.setActualStartTime(now);
        order.setCurrentNode("处理中");
        order.setProgress(30);
        order.setDisposalPlan(dto.getDisposalPlan());
        this.updateById(order);

        addFlow(order.getId(), order.getOrderCode(), 3, 2, 3,
                dto.getOperator(), dto.getOperatorDept(), dto.getOpinion() != null ? dto.getOpinion() : "开始处理",
                dto.getAttachments(), null, null);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder reportProgress(WorkOrderFlowDTO dto) {
        WorkOrder order = selectById(dto.getWorkOrderId());
        if (order.getStatus() != 3) {
            throw new BusinessException(ResultCode.STATUS_ERROR);
        }

        order.setDisposalProcess(dto.getDisposalProcess());
        order.setDuringImages(dto.getDuringImages());
        int currentProgress = order.getProgress() != null ? order.getProgress() : 30;
        order.setProgress(Math.min(currentProgress + 10, 75));
        this.updateById(order);

        addFlow(order.getId(), order.getOrderCode(), 4, 3, 3,
                dto.getOperator(), dto.getOperatorDept(), dto.getOpinion() != null ? dto.getOpinion() : "上报进度",
                dto.getAttachments(), null, null);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder applyAcceptance(WorkOrderFlowDTO dto) {
        WorkOrder order = selectById(dto.getWorkOrderId());
        if (order.getStatus() != 3) {
            throw new BusinessException(ResultCode.STATUS_ERROR);
        }

        order.setStatus(4);
        order.setCurrentNode("待验收");
        order.setProgress(85);
        order.setDisposalResult(dto.getDisposalResult());
        order.setAfterImages(dto.getAfterImages());
        this.updateById(order);

        addFlow(order.getId(), order.getOrderCode(), 5, 3, 4,
                dto.getOperator(), dto.getOperatorDept(), dto.getOpinion() != null ? dto.getOpinion() : "申请验收",
                dto.getAttachments(), dto.getNextHandler(), dto.getNextHandlerDept());

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder accept(WorkOrderFlowDTO dto) {
        WorkOrder order = selectById(dto.getWorkOrderId());
        if (order.getStatus() != 4) {
            throw new BusinessException(ResultCode.STATUS_ERROR);
        }

        String now = LocalDateTime.now().format(DTF);
        order.setStatus(5);
        order.setAcceptor(dto.getOperator());
        order.setAcceptTime(now);
        order.setAcceptOpinion(dto.getAcceptOpinion());
        order.setAcceptQualified(dto.getAcceptQualified() != null ? dto.getAcceptQualified() : 1);
        order.setCurrentNode("已完成");
        order.setProgress(100);

        if (order.getActualStartTime() != null) {
            try {
                LocalDateTime start = LocalDateTime.parse(order.getActualStartTime(), DTF);
                LocalDateTime end = LocalDateTime.parse(now, DTF);
                long minutes = Duration.between(start, end).toMinutes();
                order.setUsedHours(BigDecimal.valueOf(minutes / 60.0).setScale(2, BigDecimal.ROUND_HALF_UP));
            } catch (Exception ignored) {
            }
        }

        order.setActualCompleteTime(now);
        this.updateById(order);

        if (order.getDefectId() != null) {
            DefectReport defect = defectMapper.selectById(order.getDefectId());
            if (defect != null) {
                defect.setStatus(5);
                defectMapper.updateById(defect);
            }
        }
        if (order.getHazardId() != null) {
            Hazard hazard = hazardMapper.selectById(order.getHazardId());
            if (hazard != null) {
                hazard.setStatus(4);
                hazardMapper.updateById(hazard);
            }
        }
        if (order.getAlarmId() != null) {
            Alarm alarm = alarmMapper.selectById(order.getAlarmId());
            if (alarm != null) {
                alarm.setStatus(3);
                alarmMapper.updateById(alarm);
            }
        }

        addFlow(order.getId(), order.getOrderCode(), 7, 4, 5,
                dto.getOperator(), dto.getOperatorDept(), dto.getAcceptOpinion(),
                dto.getAttachments(), null, null);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder reject(WorkOrderFlowDTO dto) {
        WorkOrder order = selectById(dto.getWorkOrderId());

        Integer beforeStatus = order.getStatus();
        if (order.getStatus() == 4) {
            order.setStatus(3);
            order.setCurrentNode("处理中");
            order.setProgress(60);
        } else if (order.getStatus() == 2) {
            order.setStatus(1);
            order.setCurrentNode("待派单");
            order.setProgress(0);
        }

        this.updateById(order);

        addFlow(order.getId(), order.getOrderCode(), 8, beforeStatus, order.getStatus(),
                dto.getOperator(), dto.getOperatorDept(), dto.getOpinion() != null ? dto.getOpinion() : "已驳回",
                dto.getAttachments(), null, null);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder complete(WorkOrderFlowDTO dto) {
        WorkOrder order = selectById(dto.getWorkOrderId());
        order.setStatus(5);
        order.setActualCompleteTime(LocalDateTime.now().format(DTF));
        order.setCurrentNode("已完成");
        order.setProgress(100);
        this.updateById(order);

        addFlow(order.getId(), order.getOrderCode(), 9, order.getStatus(), 5,
                dto.getOperator(), dto.getOperatorDept(), dto.getOpinion() != null ? dto.getOpinion() : "工单完成",
                dto.getAttachments(), null, null);

        return order;
    }

    private void addFlow(Long workOrderId, String orderCode, Integer operationType,
                         Integer beforeStatus, Integer afterStatus, String operator,
                         String operatorDept, String opinion, String attachments,
                         String nextHandler, String nextHandlerDept) {
        WorkOrderFlow flow = new WorkOrderFlow();
        flow.setWorkOrderId(workOrderId);
        flow.setOrderCode(orderCode);
        flow.setOperationType(operationType);
        flow.setBeforeStatus(beforeStatus);
        flow.setAfterStatus(afterStatus);
        flow.setNodeName(mapNodeName(afterStatus));
        flow.setOperator(operator);
        flow.setOperatorDept(operatorDept);
        flow.setOperateTime(LocalDateTime.now().format(DTF));
        flow.setOpinion(opinion);
        flow.setAttachments(attachments);
        flow.setNextHandler(nextHandler);
        flow.setNextHandlerDept(nextHandlerDept);
        flowMapper.insert(flow);
    }

    private String mapNodeName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "待派单";
            case 2 -> "已派单";
            case 3 -> "处理中";
            case 4 -> "待验收";
            case 5 -> "已完成";
            case 6 -> "已取消";
            case 7 -> "已驳回";
            default -> "未知";
        };
    }
}
