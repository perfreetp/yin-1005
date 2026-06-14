package com.digitaltwin.pipeline.service.sensor.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.sensor.SensorQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import com.digitaltwin.pipeline.mapper.sensor.SensorMapper;
import com.digitaltwin.pipeline.service.sensor.SensorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorServiceImpl extends ServiceImpl<SensorMapper, Sensor> implements SensorService {

    @Override
    public PageResult<Sensor> selectPage(SensorQueryDTO query) {
        Page<Sensor> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getSensorType(),
                query.getAreaCode(), query.getStatus(), query.getKeyword()));
    }

    @Override
    public Sensor selectById(Long id) {
        Sensor sensor = super.getById(id);
        if (sensor == null) {
            throw new BusinessException(ResultCode.SENSOR_NOT_FOUND);
        }
        return sensor;
    }

    @Override
    public List<Sensor> selectByAreaAndType(String areaCode, Integer sensorType) {
        return baseMapper.selectByAreaAndType(areaCode, sensorType);
    }
}
