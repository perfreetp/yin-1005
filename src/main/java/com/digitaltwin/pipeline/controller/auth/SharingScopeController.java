package com.digitaltwin.pipeline.controller.auth;

import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.entity.auth.SharingScope;
import com.digitaltwin.pipeline.service.auth.SharingScopeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "共享范围控制")
@RestController
@RequestMapping("/auth/sharing")
@RequiredArgsConstructor
public class SharingScopeController {

    private final SharingScopeService sharingService;

    @Operation(summary = "查询目标的共享权限")
    @GetMapping("/target")
    public Result<List<SharingScope>> getByTarget(@RequestParam Integer shareType,
                                                  @RequestParam Long targetId) {
        return Result.success(sharingService.selectByTarget(shareType, targetId));
    }

    @Operation(summary = "查询资源的共享范围")
    @GetMapping("/resource")
    public Result<List<SharingScope>> getByResource(@RequestParam Integer resourceType,
                                                    @RequestParam(required = false, defaultValue = "0") Long resourceId) {
        return Result.success(sharingService.selectByResource(resourceType, resourceId));
    }

    @Operation(summary = "授予权限")
    @PostMapping("/grant")
    public Result<Void> grant(@RequestBody SharingScope scope) {
        sharingService.grantPermission(scope);
        return Result.success();
    }

    @Operation(summary = "撤销权限")
    @PostMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id) {
        sharingService.revokePermission(id);
        return Result.success();
    }

    @Operation(summary = "检查用户权限")
    @GetMapping("/check")
    public Result<Boolean> checkPermission(@RequestParam Long userId,
                                           @RequestParam(required = false) Integer resourceType,
                                           @RequestParam(required = false) Long resourceId,
                                           @RequestParam(required = false) String areaCode,
                                           @RequestParam(required = false) Integer pipelineType,
                                           @RequestParam(defaultValue = "1") Integer permissionType) {
        return Result.success(sharingService.hasPermission(userId, resourceType, resourceId,
                areaCode, pipelineType, permissionType));
    }
}
