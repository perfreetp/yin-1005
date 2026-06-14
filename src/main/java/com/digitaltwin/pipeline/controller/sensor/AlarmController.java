package com.digitaltwin.pipeline.controller.sensor;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.sensor.AlarmDisposalSuggestionDTO;
import com.digitaltwin.pipeline.dto.sensor.AlarmQueryDTO;
import com.digitaltwin.pipeline.dto.situation.EmergencyPlanComparisonDTO;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.entity.situation.PlanExecutionRecord;
import com.digitaltwin.pipeline.service.sensor.AlarmDisposalService;
import com.digitaltwin.pipeline.service.sensor.AlarmService;
import com.digitaltwin.pipeline.service.sensor.EmergencyPlanService;
import com.digitaltwin.pipeline.service.situation.PlanExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "告警管理")
@RestController
@RequestMapping("/sensor/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final AlarmDisposalService alarmDisposalService;
    private final EmergencyPlanService emergencyPlanService;
    private final AlarmMapper alarmMapper;
    private final PlanExecutionService planExecutionService;

    @Operation(summary = "分页查询告警记录")
    @GetMapping("/page")
    public Result<PageResult<Alarm>> page(AlarmQueryDTO query) {
        return Result.success(alarmService.selectPage(query));
    }

    @Operation(summary = "根据ID查询告警详情")
    @GetMapping("/{id}")
    public Result<Alarm> getById(@PathVariable Long id) {
        return Result.success(alarmService.selectById(id));
    }

    @Operation(summary = "处置告警")
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id,
                               @RequestParam String handler,
                               @RequestParam String handleResult) {
        alarmService.handleAlarm(id, handler, handleResult);
        return Result.success();
    }

    @Operation(summary = "【增强】获取单条告警的完整处置建议（含优先级/关阀顺序/影响区域）")
    @GetMapping("/{id}/disposal-suggestion")
    public Result<AlarmDisposalSuggestionDTO> getDisposalSuggestion(@PathVariable Long id) {
        return Result.success(alarmDisposalService.getDisposalSuggestion(id));
    }

    @Operation(summary = "【增强】获取所有待处置告警的优先级排序清单（值班大屏用）")
    @GetMapping("/pending-priority-list")
    public Result<List<AlarmDisposalSuggestionDTO>> getPendingAlarmPriorityList() {
        return Result.success(alarmDisposalService.getPendingAlarmPriorityList());
    }

    @Operation(summary = "【统一】批量查询告警详情（按ID列表）")
    @GetMapping("/batch/{ids}")
    public Result<List<Alarm>> batchGetByIds(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (idList.isEmpty()) return Result.success(Collections.emptyList());
        return Result.success(alarmMapper.selectBatchIds(idList));
    }

    @Operation(summary = "【统一】批量删除告警")
    @DeleteMapping("/batch/{ids}")
    public Result<Void> batchDelete(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (!idList.isEmpty()) alarmMapper.deleteBatchIds(idList);
        return Result.success();
    }

    @Operation(summary = "【统一】筛选查询告警（不分页，联调用）")
    @GetMapping("/filter")
    public Result<List<Alarm>> filterList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer alarmType,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) Integer alarmLevel,
                                          @RequestParam(defaultValue = "200") Integer limit) {
        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Alarm::getAlarmCode, keyword)
                    .or().like(Alarm::getTitle, keyword));
        }
        if (alarmType != null) {
            wrapper.eq(Alarm::getAlarmType, alarmType);
        }
        if (status != null) {
            wrapper.eq(Alarm::getStatus, status);
        }
        if (alarmLevel != null) {
            wrapper.eq(Alarm::getAlarmLevel, alarmLevel);
        }
        wrapper.orderByDesc(Alarm::getId);
        wrapper.last("LIMIT " + limit);
        return Result.success(alarmMapper.selectList(wrapper));
    }

    @Operation(summary = "【应急处置】获取告警的多方案对比")
    @GetMapping("/{id}/plans")
    public Result<EmergencyPlanComparisonDTO> getPlanComparison(@PathVariable Long id) {
        return Result.success(emergencyPlanService.comparePlans(id));
    }

    @Operation(summary = "【应急处置】按策略类型获取单方案详情")
    @GetMapping("/{id}/plan/{strategyType}")
    public Result<EmergencyPlanComparisonDTO.DisposalPlanVO> getSinglePlan(
            @PathVariable Long id,
            @PathVariable Integer strategyType) {
        return Result.success(emergencyPlanService.getSinglePlan(id, strategyType));
    }

    @Operation(summary = "【应急处置】选定方案并下发执行")
    @PostMapping("/{id}/select-plan")
    public Result<EmergencyPlanComparisonDTO> selectAndExecutePlan(
            @PathVariable Long id,
            @RequestParam Long planId,
            @RequestParam String operatorName) {
        return Result.success(emergencyPlanService.selectAndExecute(id, planId, operatorName));
    }

    @Operation(summary = "【应急执行】启动方案执行")
    @PostMapping("/{id}/start-plan")
    public Result<PlanExecutionRecord> startPlanExecution(
            @PathVariable Long id,
            @RequestParam Long planId,
            @RequestParam Integer strategyType,
            @RequestParam String operatorName) {
        return Result.success(planExecutionService.startExecution(id, planId, strategyType, operatorName));
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
