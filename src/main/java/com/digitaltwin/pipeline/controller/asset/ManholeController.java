package com.digitaltwin.pipeline.controller.asset;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.asset.ManholeQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import com.digitaltwin.pipeline.mapper.asset.ManholeMapper;
import com.digitaltwin.pipeline.service.asset.ManholeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "井盖管理")
@RestController
@RequestMapping("/asset/manhole")
@RequiredArgsConstructor
public class ManholeController {

    private final ManholeService manholeService;
    private final ManholeMapper manholeMapper;

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

    @Operation(summary = "【统一】批量查询井盖详情（按ID列表）")
    @GetMapping("/batch/{ids}")
    public Result<List<Manhole>> batchGetByIds(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (idList.isEmpty()) return Result.success(Collections.emptyList());
        return Result.success(manholeMapper.selectBatchIds(idList));
    }

    @Operation(summary = "【统一】批量删除井盖")
    @DeleteMapping("/batch/{ids}")
    public Result<Void> batchDelete(@PathVariable String ids) {
        List<Long> idList = parseIds(ids);
        if (!idList.isEmpty()) manholeMapper.deleteBatchIds(idList);
        return Result.success();
    }

    @Operation(summary = "【统一】按区域+管线类型筛选查询井盖（不分页，联调用）")
    @GetMapping("/filter")
    public Result<List<Manhole>> filterList(@RequestParam(required = false) String areaCode,
                                            @RequestParam(required = false) Integer pipelineType,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer condition,
                                            @RequestParam(defaultValue = "200") Integer limit) {
        LambdaQueryWrapper<Manhole> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(areaCode)) {
            wrapper.eq(Manhole::getAreaCode, areaCode);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Manhole::getManholeCode, keyword)
                    .or().like(Manhole::getRoadName, keyword));
        }
        if (condition != null) {
            wrapper.eq(Manhole::getStatus, condition);
        }
        wrapper.orderByDesc(Manhole::getId);
        wrapper.last("LIMIT " + limit);
        return Result.success(manholeMapper.selectList(wrapper));
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
