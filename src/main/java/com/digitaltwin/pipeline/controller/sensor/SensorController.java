package com.digitaltwin.pipeline.controller.sensor;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.sensor.SensorQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import com.digitaltwin.pipeline.mapper.sensor.SensorMapper;
import com.digitaltwin.pipeline.service.sensor.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "传感器管理")
@RestController
@RequestMapping("/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;
    private final SensorMapper sensorMapper;

    @Operation(summary = "分页查询传感器列表")
    @GetMapping("/page")
    public Result<PageResult<Sensor>> page(SensorQueryDTO query) {
        return Result.success(sensorService.selectPage(query));
    }

    @Operation(summary = "根据ID查询传感器详情")
    @GetMapping("/{id}")
    public Result<Sensor> getById(@PathVariable Long id) {
        return Result.success(sensorService.selectById(id));
    }

    @Operation(summary = "按区域和类型查询传感器")
    @GetMapping("/list")
    public Result<List<Sensor>> list(@RequestParam(required = false) String areaCode,
                                     @RequestParam(required = false) Integer sensorType) {
        return Result.success(sensorService.selectByAreaAndType(areaCode, sensorType));
    }

    @Operation(summary = "【统一】批量查询传感器详情（按ID列表）")
    @GetMapping("/batch/{ids}")
    public Result<List<Sensor>> batchGetByIds(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (idList.isEmpty()) return Result.success(Collections.emptyList());
        return Result.success(sensorMapper.selectBatchIds(idList));
    }

    @Operation(summary = "【统一】批量删除传感器")
    @DeleteMapping("/batch/{ids}")
    public Result<Void> batchDelete(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (!idList.isEmpty()) sensorMapper.deleteBatchIds(idList);
        return Result.success();
    }

    @Operation(summary = "【统一】按区域+管线类型筛选查询传感器（不分页，联调用）")
    @GetMapping("/filter")
    public Result<List<Sensor>> filterList(@RequestParam(required = false) String areaCode,
                                           @RequestParam(required = false) Integer pipelineType,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) Integer sensorType,
                                           @RequestParam(defaultValue = "200") Integer limit) {
        LambdaQueryWrapper<Sensor> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(areaCode)) {
            wrapper.eq(Sensor::getAreaCode, areaCode);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Sensor::getSensorCode, keyword)
                    .or().like(Sensor::getSensorName, keyword)
                    .or().like(Sensor::getInstallLocation, keyword));
        }
        if (status != null) {
            wrapper.eq(Sensor::getStatus, status);
        }
        if (sensorType != null) {
            wrapper.eq(Sensor::getSensorType, sensorType);
        }
        wrapper.orderByDesc(Sensor::getId);
        wrapper.last("LIMIT " + limit);
        return Result.success(sensorMapper.selectList(wrapper));
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
