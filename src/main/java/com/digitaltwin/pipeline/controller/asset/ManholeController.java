package com.digitaltwin.pipeline.controller.asset;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.asset.ManholeQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import com.digitaltwin.pipeline.service.asset.ManholeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "井盖管理")
@RestController
@RequestMapping("/asset/manhole")
@RequiredArgsConstructor
public class ManholeController {

    private final ManholeService manholeService;

    @Operation(summary = "分页查询井盖列表")
    @GetMapping("/page")
    public Result<PageResult<Manhole>> page(ManholeQueryDTO query) {
        return Result.success(manholeService.selectPage(query));
    }

    @Operation(summary = "根据ID查询井盖详情")
    @GetMapping("/{id}")
    public Result<Manhole> getById(@PathVariable Long id) {
        return Result.success(manholeService.selectById(id));
    }

    @Operation(summary = "按区域和类型查询井盖")
    @GetMapping("/list")
    public Result<List<Manhole>> list(@RequestParam(required = false) String areaCode,
                                      @RequestParam(required = false) Integer manholeType) {
        return Result.success(manholeService.selectByAreaAndType(areaCode, manholeType));
    }
}
