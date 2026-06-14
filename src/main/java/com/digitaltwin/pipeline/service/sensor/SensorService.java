package com.digitaltwin.pipeline.service.sensor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.sensor.SensorQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Sensor;

import java.util.List;

public interface SensorService extends IService<Sensor> {

    PageResult<Sensor> selectPage(SensorQueryDTO query);

    Sensor selectById(Long id);

    List<Sensor> selectByAreaAndType(String areaCode, Integer sensorType);
}
