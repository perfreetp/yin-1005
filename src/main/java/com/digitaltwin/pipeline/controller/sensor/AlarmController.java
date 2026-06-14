package com.digitaltwin.pipeline.controller.sensor;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.sensor.AlarmQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.service.sensor.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "告警管理")
@RestController
@RequestMapping("/sensor/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

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
}
