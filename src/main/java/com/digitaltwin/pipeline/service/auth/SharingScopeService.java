package com.digitaltwin.pipeline.service.auth;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.entity.auth.SharingScope;

import java.util.List;

public interface SharingScopeService extends IService<SharingScope> {

    List<SharingScope> selectByTarget(Integer shareType, Long targetId);

    List<SharingScope> selectByResource(Integer resourceType, Long resourceId);

    boolean grantPermission(SharingScope scope);

    boolean revokePermission(Long id);

    boolean hasPermission(Long userId, Integer resourceType, Long resourceId,
                          String areaCode, Integer pipelineType, Integer permissionType);
}
