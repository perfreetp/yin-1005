package com.digitaltwin.pipeline.service.sensor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.sensor.SensorReadingDTO;
import com.digitaltwin.pipeline.dto.sensor.SensorReadingQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.SensorReading;

public interface SensorReadingService extends IService<SensorReading> {

    PageResult<SensorReading> selectPage(SensorReadingQueryDTO query);

    boolean submitReading(SensorReadingDTO dto);
}
