package com.digitaltwin.pipeline.service.auth.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.entity.auth.SharingScope;
import com.digitaltwin.pipeline.entity.auth.SysUser;
import com.digitaltwin.pipeline.mapper.auth.SharingScopeMapper;
import com.digitaltwin.pipeline.mapper.auth.SysUserMapper;
import com.digitaltwin.pipeline.service.auth.SharingScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SharingScopeServiceImpl extends ServiceImpl<SharingScopeMapper, SharingScope>
        implements SharingScopeService {

    private final SysUserMapper userMapper;

    @Override
    public List<SharingScope> selectByTarget(Integer shareType, Long targetId) {
        return baseMapper.selectByTarget(shareType, targetId);
    }

    @Override
    public List<SharingScope> selectByResource(Integer resourceType, Long resourceId) {
        return baseMapper.selectByResource(resourceType, resourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grantPermission(SharingScope scope) {
        scope.setStatus(1);
        if (scope.getEffectiveStartTime() == null) {
            scope.setEffectiveStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return this.save(scope);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokePermission(Long id) {
        SharingScope scope = super.getById(id);
        if (scope == null) {
            return false;
        }
        scope.setStatus(2);
        return this.updateById(scope);
    }

    @Override
    public boolean hasPermission(Long userId, Integer resourceType, Long resourceId,
                                 String areaCode, Integer pipelineType, Integer permissionType) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        if (permissionType != null && permissionType == 4) {
            return true;
        }

        if (StrUtil.isNotBlank(user.getAreaCodes()) && StrUtil.isNotBlank(areaCode)) {
            List<String> userAreas = Arrays.asList(user.getAreaCodes().split(","));
            boolean areaMatch = userAreas.stream().anyMatch(a -> areaCode.startsWith(a));
            if (!areaMatch) {
                return checkSharingScope(userId, user.getDeptId(), resourceType, resourceId,
                        areaCode, pipelineType, permissionType);
            }
        }

        if (StrUtil.isNotBlank(user.getPipelineTypes()) && pipelineType != null) {
            List<String> userTypes = Arrays.asList(user.getPipelineTypes().split(","));
            if (!userTypes.contains(String.valueOf(pipelineType))) {
                return checkSharingScope(userId, user.getDeptId(), resourceType, resourceId,
                        areaCode, pipelineType, permissionType);
            }
        }

        return true;
    }

    private boolean checkSharingScope(Long userId, Long deptId, Integer resourceType, Long resourceId,
                                      String areaCode, Integer pipelineType, Integer permissionType) {
        LambdaQueryWrapper<SharingScope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SharingScope::getStatus, 1);
        wrapper.and(w -> w
                .and(w2 -> w2.eq(SharingScope::getShareType, 2).eq(SharingScope::getTargetId, userId))
                .or(w2 -> w2.eq(SharingScope::getShareType, 1).eq(deptId != null, SharingScope::getTargetId, deptId))
                .or(w2 -> w2.eq(SharingScope::getShareType, 4))
        );

        if (resourceType != null) {
            wrapper.and(w -> w.eq(SharingScope::getResourceType, 8).or().eq(SharingScope::getResourceType, resourceType));
        }
        if (resourceId != null) {
            wrapper.and(w -> w.eq(SharingScope::getResourceId, 0).or().eq(SharingScope::getResourceId, resourceId));
        }
        if (permissionType != null) {
            wrapper.and(w -> w.eq(SharingScope::getPermissionType, 4).or().eq(SharingScope::getPermissionType, permissionType));
        }

        List<SharingScope> scopes = this.list(wrapper);
        if (scopes.isEmpty()) {
            return false;
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        for (SharingScope scope : scopes) {
            if (scope.getEffectiveStartTime() != null && now.compareTo(scope.getEffectiveStartTime()) < 0) {
                continue;
            }
            if (scope.getEffectiveEndTime() != null && now.compareTo(scope.getEffectiveEndTime()) > 0) {
                continue;
            }

            if (StrUtil.isNotBlank(scope.getAreaCodes()) && StrUtil.isNotBlank(areaCode)) {
                List<String> scopeAreas = Arrays.asList(scope.getAreaCodes().split(","));
                boolean areaMatch = scopeAreas.stream().anyMatch(a -> areaCode.startsWith(a));
                if (!areaMatch) continue;
            }

            if (StrUtil.isNotBlank(scope.getPipelineTypes()) && pipelineType != null) {
                List<String> scopeTypes = Arrays.asList(scope.getPipelineTypes().split(","));
                if (!scopeTypes.contains(String.valueOf(pipelineType))) continue;
            }

            return true;
        }
        return false;
    }
}
