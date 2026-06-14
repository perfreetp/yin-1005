package com.digitaltwin.pipeline.mapper.sensor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensorMapper extends BaseMapper<Sensor> {

    IPage<Sensor> selectPageList(Page<Sensor> page,
                                 @Param("sensorType") Integer sensorType,
                                 @Param("areaCode") String areaCode,
                                 @Param("status") Integer status,
                                 @Param("keyword") String keyword);

    List<Sensor> selectByAreaAndType(@Param("areaCode") String areaCode,
                                     @Param("sensorType") Integer sensorType);
}
