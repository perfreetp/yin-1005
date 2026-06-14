package com.digitaltwin.pipeline.mapper.construction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.construction.PipelineConflict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PipelineConflictMapper extends BaseMapper<PipelineConflict> {

    List<PipelineConflict> selectByApplicationId(@Param("applicationId") Long applicationId);
}
