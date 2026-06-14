package com.digitaltwin.pipeline.mapper.inspection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.inspection.InspectionRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InspectionRouteMapper extends BaseMapper<InspectionRoute> {

    IPage<InspectionRoute> selectPageList(Page<InspectionRoute> page,
                                          @Param("routeType") Integer routeType,
                                          @Param("pipelineType") Integer pipelineType,
                                          @Param("areaCode") String areaCode,
                                          @Param("status") Integer status,
                                          @Param("keyword") String keyword);
}
