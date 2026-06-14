package com.digitaltwin.pipeline.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.auth.SysDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {

    List<SysDepartment> selectByParentId(@Param("parentId") Long parentId);
}
