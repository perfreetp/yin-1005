package com.digitaltwin.pipeline.controller.inspection;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.inspection.AdvancedInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.InsertionResultVO;
import com.digitaltwin.pipeline.dto.inspection.InspectionRouteQueryDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InsertionTask;
import com.digitaltwin.pipeline.entity.inspection.InspectionRoute;
import com.digitaltwin.pipeline.entity.inspection.ScheduleChangeLog;
import com.digitaltwin.pipeline.service.inspection.AdvancedInspectionScheduleService;
import com.digitaltwin.pipeline.service.inspection.InspectionRescheduleService;
import com.digitaltwin.pipeline.service.inspection.InspectionRouteService;
import com.digitaltwin.pipeline.service.inspection.SmartInspectionScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "巡检路线管理")
@RestController
@RequestMapping("/inspection/route")
@RequiredArgsConstructor
public class InspectionRouteController {

    private final InspectionRouteService routeService;
    private final SmartInspectionScheduleService smartScheduleService;
    private final AdvancedInspectionScheduleService advancedScheduleService;
    private final InspectionRescheduleService rescheduleService;

    @Operation(summary = "分页查询巡检路线")
    @GetMapping("/page")
    public Result<PageResult<InspectionRoute>> page(InspectionRouteQueryDTO query) {
        return Result.success(routeService.selectPage(query));
    }

    @Operation(summary = "根据ID查询巡检路线详情")
    @GetMapping("/{id}")
    public Result<InspectionRoute> getById(@PathVariable Long id) {
        return Result.success(routeService.selectById(id));
    }

    @Operation(summary = "自动生成巡检路线")
    @PostMapping("/generate")
    public Result<InspectionRoute> generate(@RequestParam(required = false) String areaCode,
                                            @RequestParam(required = false) Integer pipelineType,
                                            @RequestParam(defaultValue = "1") Integer routeType) {
        return Result.success(routeService.generateRoute(areaCode, pipelineType, routeType));
    }

    @Operation(summary = "【增强】智能巡检调度：按区域/风险/紧急程度排优先级，生成当日任务清单")
    @PostMapping("/smart-schedule")
    public Result<SmartInspectionScheduleDTO> smartSchedule(@RequestBody(required = false) SmartInspectionScheduleQueryDTO query) {
        return Result.success(smartScheduleService.generateSchedule(query));
    }

    @Operation(summary = "【高级增强】智能巡检调度：班组能力匹配、资源约束、甘特图、冲突检测")
    @PostMapping("/advanced-schedule")
    public Result<AdvancedInspectionScheduleDTO> advancedSchedule(@RequestBody(required = false) SmartInspectionScheduleQueryDTO query) {
        return Result.success(advancedScheduleService.advancedSchedule(query));
    }

    @Operation(summary = "【临时插单】执行临时插单并自动重排受影响班组")
    @PostMapping("/insert-task")
    public Result<InsertionResultVO> insertTask(@RequestBody InsertionTask task) {
        return Result.success(rescheduleService.insertTask(task));
    }

    @Operation(summary = "【临时插单】模拟插单，仅计算效果不实际生效")
    @PostMapping("/simulate-insert")
    public Result<InsertionResultVO> simulateInsert(@RequestBody InsertionTask task) {
        return Result.success(rescheduleService.simulateInsertion(task));
    }

    @Operation(summary = "【任务调整】取消任务并自动调整后续任务")
    @PostMapping("/cancel-task")
    public Result<ScheduleChangeLog> cancelTask(@RequestParam Long taskId,
                                                @RequestParam String reason,
                                                @RequestParam(required = false) String operatorName) {
        return Result.success(rescheduleService.cancelTask(taskId, reason, operatorName));
    }

    @Operation(summary = "【任务调整】调整任务负责人")
    @PostMapping("/adjust-person")
    public Result<ScheduleChangeLog> adjustPerson(@RequestParam Long taskId,
                                                  @RequestParam Long newPersonId,
                                                  @RequestParam String reason,
                                                  @RequestParam(required = false) String operatorName) {
        return Result.success(rescheduleService.adjustTaskPerson(taskId, newPersonId, reason, operatorName));
    }

    @Operation(summary = "【任务调整】调整任务车辆")
    @PostMapping("/adjust-vehicle")
    public Result<ScheduleChangeLog> adjustVehicle(@RequestParam Long taskId,
                                                   @RequestParam Long newVehicleId,
                                                   @RequestParam String reason,
                                                   @RequestParam(required = false) String operatorName) {
        return Result.success(rescheduleService.adjustTaskVehicle(taskId, newVehicleId, reason, operatorName));
    }

    @Operation(summary = "【变更日志】获取当日排班变更日志")
    @GetMapping("/change-logs")
    public Result<List<ScheduleChangeLog>> getChangeLogs(@RequestParam(required = false) String date) {
        return Result.success(rescheduleService.getDayChangeLogs(date));
    }
}
