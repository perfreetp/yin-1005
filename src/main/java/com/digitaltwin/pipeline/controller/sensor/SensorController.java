package com.digitaltwin.pipeline.controller.sensor;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.sensor.SensorQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import com.digitaltwin.pipeline.service.sensor.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "传感器管理")
@RestController
@RequestMapping("/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

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
}
