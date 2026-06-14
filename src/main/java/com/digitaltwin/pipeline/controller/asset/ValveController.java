package com.digitaltwin.pipeline.controller.asset;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.asset.ValveQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.service.asset.ValveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "阀门管理")
@RestController
@RequestMapping("/asset/valve")
@RequiredArgsConstructor
public class ValveController {

    private final ValveService valveService;
    private final ValveMapper valveMapper;

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

    @Operation(summary = "【统一】批量查询阀门详情（按ID列表）")
    @GetMapping("/batch/{ids}")
    public Result<List<Valve>> batchGetByIds(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (idList.isEmpty()) return Result.success(Collections.emptyList());
        return Result.success(valveMapper.selectBatchIds(idList));
    }

    @Operation(summary = "【统一】批量删除阀门")
    @DeleteMapping("/batch/{ids}")
    public Result<Void> batchDelete(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (!idList.isEmpty()) valveMapper.deleteBatchIds(idList);
        return Result.success();
    }

    @Operation(summary = "【统一】按区域+管线类型筛选查询阀门（不分页，联调用）")
    @GetMapping("/filter")
    public Result<List<Valve>> filterList(@RequestParam(required = false) String areaCode,
                                          @RequestParam(required = false) Integer pipelineType,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "200") Integer limit) {
        LambdaQueryWrapper<Valve> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(areaCode)) {
            wrapper.eq(Valve::getAreaCode, areaCode);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Valve::getValveCode, keyword)
                    .or().like(Valve::getValveName, keyword)
                    .or().like(Valve::getLocation, keyword));
        }
        if (status != null) {
            wrapper.eq(Valve::getStatus, status);
        }
        wrapper.orderByDesc(Valve::getId);
        wrapper.last("LIMIT " + limit);
        return Result.success(valveMapper.selectList(wrapper));
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
