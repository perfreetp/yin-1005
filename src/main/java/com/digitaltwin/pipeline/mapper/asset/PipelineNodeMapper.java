package com.digitaltwin.pipeline.mapper.asset;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.asset.PipelineNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PipelineNodeMapper extends BaseMapper<PipelineNode> {

    List<PipelineNode> selectByAreaCode(@Param("areaCode") String areaCode);
}
