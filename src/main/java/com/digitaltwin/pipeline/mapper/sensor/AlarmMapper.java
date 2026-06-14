package com.digitaltwin.pipeline.mapper.sensor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlarmMapper extends BaseMapper<Alarm> {

    IPage<Alarm> selectPageList(Page<Alarm> page,
                                @Param("alarmType") Integer alarmType,
                                @Param("alarmLevel") Integer alarmLevel,
                                @Param("areaCode") String areaCode,
                                @Param("status") Integer status,
                                @Param("startTime") String startTime,
                                @Param("endTime") String endTime);
}
