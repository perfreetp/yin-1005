package com.digitaltwin.pipeline.controller.trace;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.trace.ChangeHistoryQueryDTO;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;
import com.digitaltwin.pipeline.service.trace.ChangeHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "查询指定业务的变更历史")
    @GetMapping("/business")
    public Result<List<ChangeHistory>> getByBusiness(@RequestParam Integer businessType,
                                                     @RequestParam Long businessId) {
        return Result.success(historyService.selectByBusiness(businessType, businessId));
    }
}
