package com.digitaltwin.pipeline.controller.inspection;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderFlowDTO;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.inspection.WorkOrderFlow;
import com.digitaltwin.pipeline.service.inspection.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "维修工单管理")
@RestController
@RequestMapping("/inspection/work-order")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @Operation(summary = "分页查询工单列表")
    @GetMapping("/page")
    public Result<PageResult<WorkOrder>> page(WorkOrderQueryDTO query) {
        return Result.success(workOrderService.selectPage(query));
    }

    @Operation(summary = "根据ID查询工单详情")
    @GetMapping("/{id}")
    public Result<WorkOrder> getById(@PathVariable Long id) {
        return Result.success(workOrderService.selectById(id));
    }

    @Operation(summary = "根据缺陷创建工单")
    @PostMapping("/from-defect/{defectId}")
    public Result<WorkOrder> createFromDefect(@PathVariable Long defectId) {
        return Result.success(workOrderService.createFromDefect(defectId));
    }

    @Operation(summary = "根据隐患创建工单")
    @PostMapping("/from-hazard/{hazardId}")
    public Result<WorkOrder> createFromHazard(@PathVariable Long hazardId) {
        return Result.success(workOrderService.createFromHazard(hazardId));
    }

    @Operation(summary = "根据告警创建工单")
    @PostMapping("/from-alarm/{alarmId}")
    public Result<WorkOrder> createFromAlarm(@PathVariable Long alarmId) {
        return Result.success(workOrderService.createFromAlarm(alarmId));
    }

    @Operation(summary = "查询工单流转记录")
    @GetMapping("/{id}/flows")
    public Result<List<WorkOrderFlow>> getFlows(@PathVariable Long id) {
        return Result.success(workOrderService.getFlows(id));
    }

    @Operation(summary = "派单")
    @PostMapping("/dispatch")
    public Result<WorkOrder> dispatch(@RequestBody WorkOrderFlowDTO dto) {
        return Result.success(workOrderService.dispatch(dto));
    }

    @Operation(summary = "开始处理")
    @PostMapping("/start")
    public Result<WorkOrder> startProcess(@RequestBody WorkOrderFlowDTO dto) {
        return Result.success(workOrderService.startProcess(dto));
    }

    @Operation(summary = "上报进度")
    @PostMapping("/progress")
    public Result<WorkOrder> reportProgress(@RequestBody WorkOrderFlowDTO dto) {
        return Result.success(workOrderService.reportProgress(dto));
    }

    @Operation(summary = "申请验收")
    @PostMapping("/apply-acceptance")
    public Result<WorkOrder> applyAcceptance(@RequestBody WorkOrderFlowDTO dto) {
        return Result.success(workOrderService.applyAcceptance(dto));
    }

    @Operation(summary = "验收通过")
    @PostMapping("/accept")
    public Result<WorkOrder> accept(@RequestBody WorkOrderFlowDTO dto) {
        return Result.success(workOrderService.accept(dto));
    }

    @Operation(summary = "驳回")
    @PostMapping("/reject")
    public Result<WorkOrder> reject(@RequestBody WorkOrderFlowDTO dto) {
        return Result.success(workOrderService.reject(dto));
    }

    @Operation(summary = "完成工单")
    @PostMapping("/complete")
    public Result<WorkOrder> complete(@RequestBody WorkOrderFlowDTO dto) {
        return Result.success(workOrderService.complete(dto));
    }
}
