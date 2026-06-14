package com.digitaltwin.pipeline.service.sensor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.sensor.AlarmQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Alarm;

public interface AlarmService extends IService<Alarm> {

    PageResult<Alarm> selectPage(AlarmQueryDTO query);

    Alarm selectById(Long id);

    boolean handleAlarm(Long id, String handler, String handleResult);
}
