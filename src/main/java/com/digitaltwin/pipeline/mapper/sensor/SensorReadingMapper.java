package com.digitaltwin.pipeline.mapper.sensor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.sensor.SensorReading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SensorReadingMapper extends BaseMapper<SensorReading> {

    IPage<SensorReading> selectPageList(Page<SensorReading> page,
                                        @Param("sensorId") Long sensorId,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);
}
