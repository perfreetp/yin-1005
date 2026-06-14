package com.digitaltwin.pipeline.controller.sensor;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.sensor.HazardDTO;
import com.digitaltwin.pipeline.dto.sensor.HazardQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.service.sensor.HazardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "隐患点管理")
@RestController
@RequestMapping("/sensor/hazard")
@RequiredArgsConstructor
public class HazardController {

    private final HazardService hazardService;
    private final HazardMapper hazardMapper;

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

    @Operation(summary = "【统一】批量查询隐患点详情（按ID列表）")
    @GetMapping("/batch/{ids}")
    public Result<List<Hazard>> batchGetByIds(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (idList.isEmpty()) return Result.success(Collections.emptyList());
        return Result.success(hazardMapper.selectBatchIds(idList));
    }

    @Operation(summary = "【统一】批量删除隐患点")
    @DeleteMapping("/batch/{ids}")
    public Result<Void> batchDelete(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (!idList.isEmpty()) hazardMapper.deleteBatchIds(idList);
        return Result.success();
    }

    @Operation(summary = "【统一】按区域+管线类型筛选查询隐患点（不分页，联调用）")
    @GetMapping("/filter")
    public Result<List<Hazard>> filterList(@RequestParam(required = false) String areaCode,
                                           @RequestParam(required = false) Integer pipelineType,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) Integer riskLevel,
                                           @RequestParam(defaultValue = "200") Integer limit) {
        LambdaQueryWrapper<Hazard> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(areaCode)) {
            wrapper.eq(Hazard::getAreaCode, areaCode);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Hazard::getHazardCode, keyword)
                    .or().like(Hazard::getLocation, keyword)
                    .or().like(Hazard::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(Hazard::getStatus, status);
        }
        if (riskLevel != null) {
            wrapper.eq(Hazard::getRiskLevel, riskLevel);
        }
        wrapper.orderByDesc(Hazard::getId);
        wrapper.last("LIMIT " + limit);
        return Result.success(hazardMapper.selectList(wrapper));
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
