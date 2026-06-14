package com.digitaltwin.pipeline.mapper.asset;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.asset.Valve;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ValveMapper extends BaseMapper<Valve> {

    IPage<Valve> selectPageList(Page<Valve> page,
                                @Param("valveType") Integer valveType,
                                @Param("areaCode") String areaCode,
                                @Param("status") Integer status,
                                @Param("keyword") String keyword);

    List<Valve> selectByPipelineId(@Param("pipelineId") Long pipelineId);

    List<Valve> selectEmergencyValves(@Param("lng") Double lng,
                                      @Param("lat") Double lat,
                                      @Param("pipelineType") Integer pipelineType,
                                      @Param("radius") Double radius);
}
