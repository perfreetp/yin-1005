package com.digitaltwin.pipeline.controller.sensor;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.sensor.SensorReadingDTO;
import com.digitaltwin.pipeline.dto.sensor.SensorReadingQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.SensorReading;
import com.digitaltwin.pipeline.service.sensor.SensorReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "传感器读数")
@RestController
@RequestMapping("/sensor/reading")
@RequiredArgsConstructor
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;

    @Operation(summary = "分页查询传感器读数记录")
    @GetMapping("/page")
    public Result<PageResult<SensorReading>> page(SensorReadingQueryDTO query) {
        return Result.success(sensorReadingService.selectPage(query));
    }

    @Operation(summary = "上报传感器读数")
    @PostMapping
    public Result<Void> submit(@Valid @RequestBody SensorReadingDTO dto) {
        sensorReadingService.submitReading(dto);
        return Result.success();
    }
}
