package com.digitaltwin.pipeline.controller.situation;

import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.situation.CommandDashboardVO;
import com.digitaltwin.pipeline.service.situation.CommandDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "指挥大屏（多事件对比+优先级排序）")
@RestController
@RequestMapping("/situation/command-dashboard")
@RequiredArgsConstructor
public class CommandDashboardController {

    private final CommandDashboardService commandDashboardService;

    @Operation(summary = "指挥大屏总览（多事件并排对比+全局指标+排行榜+时间轴+区域/类型对比）")
    @GetMapping
    public Result<CommandDashboardVO> dashboard(
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) Integer minLevel,
            @RequestParam(required = false) List<Integer> eventTypes) {
        return Result.success(commandDashboardService.getDashboard(areaCode, minLevel, eventTypes));
    }
}
