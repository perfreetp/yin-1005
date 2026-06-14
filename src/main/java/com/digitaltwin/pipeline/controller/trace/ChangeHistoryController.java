package com.digitaltwin.pipeline.controller.trace;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.trace.ChangeHistoryQueryDTO;
import com.digitaltwin.pipeline.dto.trace.ChangeTimelineDTO;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;
import com.digitaltwin.pipeline.service.trace.ChangeHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "历史变更追溯")
@RestController
@RequestMapping("/trace/history")
@RequiredArgsConstructor
public class ChangeHistoryController {

    private final ChangeHistoryService historyService;

    @Operation(summary = "分页查询变更历史")
    @GetMapping("/page")
    public Result<PageResult<ChangeHistory>> page(ChangeHistoryQueryDTO query) {
        return Result.success(historyService.selectPage(query));
    }

    @Operation(summary = "【增强】按资源查询完整变更时间线（含字段差异对比、谁改了什么、统计汇总）")
    @GetMapping("/timeline")
    public Result<ChangeTimelineDTO> timeline(@RequestParam Integer resourceType,
                                              @RequestParam Long resourceId) {
        return Result.success(historyService.getResourceTimeline(resourceType, resourceId));
    }
}
