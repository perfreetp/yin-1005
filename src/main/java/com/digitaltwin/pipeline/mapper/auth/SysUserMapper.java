package com.digitaltwin.pipeline.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.auth.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUser> selectPageList(Page<SysUser> page,
                                  @Param("deptId") Long deptId,
                                  @Param("status") Integer status,
                                  @Param("keyword") String keyword);

    SysUser selectByUsername(@Param("username") String username);
}
