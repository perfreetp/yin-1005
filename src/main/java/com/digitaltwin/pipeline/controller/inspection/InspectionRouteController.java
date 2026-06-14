package com.digitaltwin.pipeline.controller.inspection;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.inspection.InspectionRouteQueryDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InspectionRoute;
import com.digitaltwin.pipeline.service.inspection.InspectionRouteService;
import com.digitaltwin.pipeline.service.inspection.SmartInspectionScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "巡检路线管理")
@RestController
@RequestMapping("/inspection/route")
@RequiredArgsConstructor
public class InspectionRouteController {

    private final InspectionRouteService routeService;
    private final SmartInspectionScheduleService smartScheduleService;

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
}
