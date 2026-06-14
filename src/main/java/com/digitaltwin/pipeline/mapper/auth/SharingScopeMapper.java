package com.digitaltwin.pipeline.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.auth.SharingScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SharingScopeMapper extends BaseMapper<SharingScope> {

    List<SharingScope> selectByTarget(@Param("shareType") Integer shareType,
                                      @Param("targetId") Long targetId);

    List<SharingScope> selectByResource(@Param("resourceType") Integer resourceType,
                                        @Param("resourceId") Long resourceId);
}
