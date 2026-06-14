package com.digitaltwin.pipeline.controller.common;

import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.common.UnifiedListResult;
import com.digitaltwin.pipeline.dto.common.UnifiedListItemVO;
import com.digitaltwin.pipeline.service.common.UnifiedQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "统一查询接口（三端联动共用）")
@RestController
@RequestMapping("/common/unified")
@RequiredArgsConstructor
public class UnifiedQueryController {

    private final UnifiedQueryService unifiedQueryService;

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
}
