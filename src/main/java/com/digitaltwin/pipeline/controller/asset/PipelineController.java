package com.digitaltwin.pipeline.controller.asset;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.asset.PipelineDTO;
import com.digitaltwin.pipeline.dto.asset.PipelineQueryDTO;
import com.digitaltwin.pipeline.dto.asset.TopologyResultDTO;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.service.asset.PipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管线资产管理")
@RestController
@RequestMapping("/asset/pipeline")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;

    @Operation(summary = "分页查询管线列表")
    @GetMapping("/page")
    public Result<PageResult<Pipeline>> page(PipelineQueryDTO query) {
        return Result.success(pipelineService.selectPage(query));
    }

    @Operation(summary = "根据ID查询管线详情")
    @GetMapping("/{id}")
    public Result<Pipeline> getById(@PathVariable Long id) {
        return Result.success(pipelineService.selectById(id));
    }

    @Operation(summary = "新增管线")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody PipelineDTO dto) {
        pipelineService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新管线")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody PipelineDTO dto) {
        pipelineService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除管线")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        pipelineService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "按区域和类型查询管线")
    @GetMapping("/list")
    public Result<List<Pipeline>> list(@RequestParam(required = false) String areaCode,
                                       @RequestParam(required = false) Integer pipelineType) {
        return Result.success(pipelineService.selectByAreaAndType(areaCode, pipelineType));
    }

    @Operation(summary = "管网拓扑查询")
    @GetMapping("/topology")
    public Result<TopologyResultDTO> topology(@RequestParam(required = false) String areaCode,
                                              @RequestParam(required = false) Integer pipelineType,
                                              @RequestParam(required = false) String nodeCode) {
        return Result.success(pipelineService.queryTopology(areaCode, pipelineType, nodeCode));
    }
}
