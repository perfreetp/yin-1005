package com.digitaltwin.pipeline.controller.asset;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.asset.ValveQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.service.asset.ValveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "阀门管理")
@RestController
@RequestMapping("/asset/valve")
@RequiredArgsConstructor
public class ValveController {

    private final ValveService valveService;

    @Operation(summary = "分页查询阀门列表")
    @GetMapping("/page")
    public Result<PageResult<Valve>> page(ValveQueryDTO query) {
        return Result.success(valveService.selectPage(query));
    }

    @Operation(summary = "根据ID查询阀门详情")
    @GetMapping("/{id}")
    public Result<Valve> getById(@PathVariable Long id) {
        return Result.success(valveService.selectById(id));
    }

    @Operation(summary = "根据管线ID查询关联阀门")
    @GetMapping("/by-pipeline/{pipelineId}")
    public Result<List<Valve>> getByPipelineId(@PathVariable Long pipelineId) {
        return Result.success(valveService.selectByPipelineId(pipelineId));
    }

    @Operation(summary = "查询应急关阀建议（附近阀门）")
    @GetMapping("/emergency")
    public Result<List<Valve>> getEmergencyValves(@RequestParam Double lng,
                                                  @RequestParam Double lat,
                                                  @RequestParam Integer pipelineType,
                                                  @RequestParam(defaultValue = "500") Double radius) {
        return Result.success(valveService.selectEmergencyValves(lng, lat, pipelineType, radius));
    }
}
