package com.digitaltwin.pipeline.mapper.sensor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HazardMapper extends BaseMapper<Hazard> {

    IPage<Hazard> selectPageList(Page<Hazard> page,
                                 @Param("hazardType") Integer hazardType,
                                 @Param("riskLevel") Integer riskLevel,
                                 @Param("areaCode") String areaCode,
                                 @Param("status") Integer status,
                                 @Param("keyword") String keyword);
}
