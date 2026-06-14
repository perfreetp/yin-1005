package com.digitaltwin.pipeline.controller.inspection;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.inspection.InspectionRecordQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InspectionRecord;
import com.digitaltwin.pipeline.service.inspection.InspectionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "巡检记录管理")
@RestController
@RequestMapping("/inspection/record")
@RequiredArgsConstructor
public class InspectionRecordController {

    private final InspectionRecordService recordService;

    @Operation(summary = "分页查询巡检记录")
    @GetMapping("/page")
    public Result<PageResult<InspectionRecord>> page(InspectionRecordQueryDTO query) {
        return Result.success(recordService.selectPage(query));
    }

    @Operation(summary = "根据ID查询巡检记录详情")
    @GetMapping("/{id}")
    public Result<InspectionRecord> getById(@PathVariable Long id) {
        return Result.success(recordService.selectById(id));
    }

    @Operation(summary = "开始巡检")
    @PostMapping("/start")
    public Result<InspectionRecord> start(@RequestParam Long routeId,
                                          @RequestParam Long inspectorId,
                                          @RequestParam String inspectorName) {
        return Result.success(recordService.startInspection(routeId, inspectorId, inspectorName));
    }

    @Operation(summary = "结束巡检")
    @PostMapping("/{id}/end")
    public Result<InspectionRecord> end(@PathVariable Long id,
                                        @RequestParam(required = false) String trajectory,
                                        @RequestParam(required = false) Integer checkedPoints,
                                        @RequestParam(required = false) BigDecimal actualDistance,
                                        @RequestParam(required = false) Integer defectCount,
                                        @RequestParam(required = false) Integer reportedDefectCount) {
        return Result.success(recordService.endInspection(id, trajectory, checkedPoints,
                actualDistance, defectCount, reportedDefectCount));
    }
}
