package com.digitaltwin.pipeline.service.inspection;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderFlowDTO;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.inspection.WorkOrderFlow;

import java.util.List;

public interface WorkOrderService extends IService<WorkOrder> {

    PageResult<WorkOrder> selectPage(WorkOrderQueryDTO query);

    WorkOrder selectById(Long id);

    WorkOrder createFromDefect(Long defectId);

    WorkOrder createFromHazard(Long hazardId);

    WorkOrder createFromAlarm(Long alarmId);

    List<WorkOrderFlow> getFlows(Long workOrderId);

    WorkOrder dispatch(WorkOrderFlowDTO dto);

    WorkOrder startProcess(WorkOrderFlowDTO dto);

    WorkOrder reportProgress(WorkOrderFlowDTO dto);

    WorkOrder applyAcceptance(WorkOrderFlowDTO dto);

    WorkOrder accept(WorkOrderFlowDTO dto);

    WorkOrder reject(WorkOrderFlowDTO dto);

    WorkOrder complete(WorkOrderFlowDTO dto);
}
