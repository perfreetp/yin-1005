package com.digitaltwin.pipeline.mapper.asset;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PipelineMapper extends BaseMapper<Pipeline> {

    IPage<Pipeline> selectPageList(Page<Pipeline> page,
                                   @Param("pipelineType") Integer pipelineType,
                                   @Param("areaCode") String areaCode,
                                   @Param("status") Integer status,
                                   @Param("keyword") String keyword);

    List<Pipeline> selectByAreaAndType(@Param("areaCode") String areaCode,
                                       @Param("pipelineType") Integer pipelineType);

    List<Pipeline> selectConnectedPipelines(@Param("nodeCode") String nodeCode);
}
