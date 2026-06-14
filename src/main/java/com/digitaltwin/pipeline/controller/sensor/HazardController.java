package com.digitaltwin.pipeline.controller.sensor;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.sensor.HazardDTO;
import com.digitaltwin.pipeline.dto.sensor.HazardQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.service.sensor.HazardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "隐患点管理")
@RestController
@RequestMapping("/sensor/hazard")
@RequiredArgsConstructor
public class HazardController {

    private final HazardService hazardService;

    @Operation(summary = "分页查询隐患点列表")
    @GetMapping("/page")
    public Result<PageResult<Hazard>> page(HazardQueryDTO query) {
        return Result.success(hazardService.selectPage(query));
    }

    @Operation(summary = "根据ID查询隐患点详情")
    @GetMapping("/{id}")
    public Result<Hazard> getById(@PathVariable Long id) {
        return Result.success(hazardService.selectById(id));
    }

    @Operation(summary = "新增隐患点")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody HazardDTO dto) {
        hazardService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新隐患点")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody HazardDTO dto) {
        hazardService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除隐患点")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        hazardService.deleteById(id);
        return Result.success();
    }
}
