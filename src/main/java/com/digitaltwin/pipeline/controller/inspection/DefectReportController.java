package com.digitaltwin.pipeline.controller.inspection;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.inspection.DefectQueryDTO;
import com.digitaltwin.pipeline.dto.inspection.DefectReportDTO;
import com.digitaltwin.pipeline.entity.inspection.DefectReport;
import com.digitaltwin.pipeline.service.inspection.DefectReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "缺陷上报管理")
@RestController
@RequestMapping("/inspection/defect")
@RequiredArgsConstructor
public class DefectReportController {

    private final DefectReportService defectService;

    @Operation(summary = "分页查询缺陷上报记录")
    @GetMapping("/page")
    public Result<PageResult<DefectReport>> page(DefectQueryDTO query) {
        return Result.success(defectService.selectPage(query));
    }

    @Operation(summary = "根据ID查询缺陷详情")
    @GetMapping("/{id}")
    public Result<DefectReport> getById(@PathVariable Long id) {
        return Result.success(defectService.selectById(id));
    }

    @Operation(summary = "上报缺陷")
    @PostMapping
    public Result<DefectReport> report(@Valid @RequestBody DefectReportDTO dto) {
        return Result.success(defectService.report(dto));
    }

    @Operation(summary = "受理缺陷")
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id, @RequestParam String receiver) {
        defectService.receive(id, receiver);
        return Result.success();
    }
}
