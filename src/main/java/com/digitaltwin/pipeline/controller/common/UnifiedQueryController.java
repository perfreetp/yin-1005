package com.digitaltwin.pipeline.controller.common;

import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.common.BatchQueryItem;
import com.digitaltwin.pipeline.dto.common.UnifiedDetailVO;
import com.digitaltwin.pipeline.dto.common.UnifiedListResult;
import com.digitaltwin.pipeline.dto.common.UnifiedListItemVO;
import com.digitaltwin.pipeline.dto.common.UnifiedTimelineItemVO;
import com.digitaltwin.pipeline.service.common.UnifiedDetailService;
import com.digitaltwin.pipeline.service.common.UnifiedQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "统一查询接口（三端联动共用，列表/详情/时间线/批量 全部同构）")
@RestController
@RequestMapping("/common/unified")
@RequiredArgsConstructor
public class UnifiedQueryController {

    private final UnifiedQueryService unifiedQueryService;
    private final UnifiedDetailService unifiedDetailService;

    @Operation(summary = "多源混合列表查询（告警/事件/工单/开挖/隐患 统一格式，一张图/大屏直接用）")
    @GetMapping("/mixed-list")
    public Result<UnifiedListResult<UnifiedListItemVO>> mixedList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) Integer pipelineType,
            @RequestParam(required = false) List<Integer> resourceTypes,
            @RequestParam(required = false) Integer minPriority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "priority") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        return Result.success(unifiedQueryService.queryMixedList(
                pageNum, pageSize, areaCode, pipelineType, resourceTypes, minPriority, keyword, sortField, sortOrder));
    }

    @Operation(summary = "统一详情（告警/事件/工单/开挖/隐患 全部同一结构，和列表项字段一一对应）")
    @GetMapping("/detail")
    public Result<UnifiedDetailVO> detail(
            @RequestParam Integer resourceType,
            @RequestParam Long resourceId) {
        return Result.success(unifiedDetailService.getDetail(resourceType, resourceId));
    }

    @Operation(summary = "批量查询详情（多个不同类型资源一次查完，返回和详情同结构的列表）")
    @PostMapping("/batch-details")
    public Result<List<UnifiedDetailVO>> batchDetails(@RequestBody List<BatchQueryItem> items) {
        return Result.success(unifiedDetailService.batchGetDetails(items));
    }

    @Operation(summary = "统一时间线（变更/流转/日志 全部同一格式，和详情页时间线完全一致）")
    @GetMapping("/timeline")
    public Result<List<UnifiedTimelineItemVO>> timeline(
            @RequestParam Integer resourceType,
            @RequestParam Long resourceId,
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(unifiedDetailService.getTimeline(resourceType, resourceId, limit));
    }
}
