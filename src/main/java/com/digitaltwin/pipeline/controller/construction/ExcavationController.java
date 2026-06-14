package com.digitaltwin.pipeline.controller.construction;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.construction.ExcavationDTO;
import com.digitaltwin.pipeline.dto.construction.ExcavationImpactAssessmentDTO;
import com.digitaltwin.pipeline.dto.construction.ExcavationQueryDTO;
import com.digitaltwin.pipeline.dto.construction.ExcavationReviewResultDTO;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.construction.PipelineConflict;
import com.digitaltwin.pipeline.mapper.construction.PipelineConflictMapper;
import com.digitaltwin.pipeline.service.construction.ExcavationApplicationService;
import com.digitaltwin.pipeline.service.construction.ExcavationImpactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "施工审批管理")
@RestController
@RequestMapping("/construction/excavation")
@RequiredArgsConstructor
public class ExcavationController {

    private final ExcavationApplicationService excavationService;
    private final PipelineConflictMapper conflictMapper;
    private final ExcavationImpactService impactService;

    @Operation(summary = "分页查询开挖申请列表")
    @GetMapping("/page")
    public Result<PageResult<ExcavationApplication>> page(ExcavationQueryDTO query) {
        return Result.success(excavationService.selectPage(query));
    }

    @Operation(summary = "根据ID查询开挖申请详情")
    @GetMapping("/{id}")
    public Result<ExcavationApplication> getById(@PathVariable Long id) {
        return Result.success(excavationService.selectById(id));
    }

    @Operation(summary = "新增开挖申请")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ExcavationDTO dto) {
        excavationService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新开挖申请")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ExcavationDTO dto) {
        excavationService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除开挖申请")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        excavationService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "开挖申请校核（管线冲突检测+影响评估+关阀建议）")
    @PostMapping("/{id}/review")
    public Result<ExcavationReviewResultDTO> review(@PathVariable Long id) {
        return Result.success(excavationService.review(id));
    }

    @Operation(summary = "审批开挖申请")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                                @RequestParam Integer passed,
                                @RequestParam String opinion,
                                @RequestParam String reviewer) {
        excavationService.approve(id, passed, opinion, reviewer);
        return Result.success();
    }

    @Operation(summary = "查询开挖申请的管线冲突列表")
    @GetMapping("/{id}/conflicts")
    public Result<List<PipelineConflict>> getConflicts(@PathVariable Long id) {
        return Result.success(conflictMapper.selectByApplicationId(id));
    }

    @Operation(summary = "【增强】开挖影响范围综合评估（含管线/阀门/井盖/道路/部门完整信息）")
    @GetMapping("/{id}/impact-assessment")
    public Result<ExcavationImpactAssessmentDTO> impactAssessment(@PathVariable Long id) {
        return Result.success(impactService.comprehensiveAssessment(id));
    }

    @Operation(summary = "【增强】生成影响评估文字报告")
    @GetMapping("/{id}/impact-report")
    public Result<String> impactReport(@PathVariable Long id) {
        return Result.success(impactService.generateImpactReport(id));
    }

    @Operation(summary = "【增强】提交审批（自动触发综合评估）")
    @PostMapping("/{id}/submit")
    public Result<ExcavationImpactAssessmentDTO> submitForReview(@PathVariable Long id) {
        impactService.submitForReview(id);
        return Result.success(impactService.comprehensiveAssessment(id));
    }
}
