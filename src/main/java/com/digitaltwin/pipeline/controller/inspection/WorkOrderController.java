package com.digitaltwin.pipeline.controller.inspection;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderFlowDTO;
import com.digitaltwin.pipeline.dto.inspection.WorkOrderQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import com.digitaltwin.pipeline.entity.inspection.WorkOrderFlow;
import com.digitaltwin.pipeline.mapper.inspection.WorkOrderMapper;
import com.digitaltwin.pipeline.service.inspection.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "维修工单管理")
@RestController
@RequestMapping("/inspection/work-order")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final WorkOrderMapper workOrderMapper;

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

    @Operation(summary = "【统一】批量查询工单详情（按ID列表）")
    @GetMapping("/batch/{ids}")
    public Result<List<WorkOrder>> batchGetByIds(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (idList.isEmpty()) return Result.success(Collections.emptyList());
        return Result.success(workOrderMapper.selectBatchIds(idList));
    }

    @Operation(summary = "【统一】批量删除工单")
    @DeleteMapping("/batch/{ids}")
    public Result<Void> batchDelete(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (!idList.isEmpty()) workOrderMapper.deleteBatchIds(idList);
        return Result.success();
    }

    @Operation(summary = "【统一】按区域+管线类型筛选查询工单（不分页，联调用）")
    @GetMapping("/filter")
    public Result<List<WorkOrder>> filterList(@RequestParam(required = false) String areaCode,
                                              @RequestParam(required = false) Integer pipelineType,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Integer urgency,
                                              @RequestParam(defaultValue = "200") Integer limit) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(areaCode)) {
            wrapper.eq(WorkOrder::getAreaCode, areaCode);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(WorkOrder::getOrderCode, keyword)
                    .or().like(WorkOrder::getTitle, keyword)
                    .or().like(WorkOrder::getLocation, keyword));
        }
        if (status != null) {
            wrapper.eq(WorkOrder::getStatus, status);
        }
        if (urgency != null) {
            wrapper.eq(WorkOrder::getUrgency, urgency);
        }
        wrapper.orderByDesc(WorkOrder::getId);
        wrapper.last("LIMIT " + limit);
        return Result.success(workOrderMapper.selectList(wrapper));
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
