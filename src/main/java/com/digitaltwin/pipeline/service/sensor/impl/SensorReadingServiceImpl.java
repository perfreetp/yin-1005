package com.digitaltwin.pipeline.service.sensor.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.sensor.SensorReadingDTO;
import com.digitaltwin.pipeline.dto.sensor.SensorReadingQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.entity.sensor.Sensor;
import com.digitaltwin.pipeline.entity.sensor.SensorReading;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.mapper.sensor.SensorMapper;
import com.digitaltwin.pipeline.mapper.sensor.SensorReadingMapper;
import com.digitaltwin.pipeline.service.sensor.SensorReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SensorReadingServiceImpl extends ServiceImpl<SensorReadingMapper, SensorReading> implements SensorReadingService {

    private final SensorMapper sensorMapper;
    private final AlarmMapper alarmMapper;

    @Override
    public PageResult<SensorReading> selectPage(SensorReadingQueryDTO query) {
        Page<SensorReading> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getSensorId(),
                query.getStartTime(), query.getEndTime()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitReading(SensorReadingDTO dto) {
        Sensor sensor = sensorMapper.selectById(dto.getSensorId());
        if (sensor == null) {
            throw new BusinessException(ResultCode.SENSOR_NOT_FOUND);
        }

        SensorReading reading = new SensorReading();
        reading.setSensorId(dto.getSensorId());
        reading.setSensorCode(sensor.getSensorCode());
        reading.setSensorType(sensor.getSensorType());
        reading.setReadingValue(dto.getReadingValue());
        reading.setUnit(sensor.getUnit());
        reading.setCollectTime(dto.getCollectTime() != null ? dto.getCollectTime() :
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        reading.setPipelineId(sensor.getPipelineId());
        reading.setAreaCode(sensor.getAreaCode());

        boolean isAlarm = false;
        Integer alarmLevel = 0;

        BigDecimal value = dto.getReadingValue();
        if (sensor.getAlarmUpper() != null && value.compareTo(sensor.getAlarmUpper()) > 0) {
            isAlarm = true;
            BigDecimal diff = value.subtract(sensor.getAlarmUpper());
            BigDecimal ratio = diff.divide(sensor.getAlarmUpper(), 4, BigDecimal.ROUND_HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.5")) >= 0) {
                alarmLevel = 4;
            } else if (ratio.compareTo(new BigDecimal("0.3")) >= 0) {
                alarmLevel = 3;
            } else if (ratio.compareTo(new BigDecimal("0.1")) >= 0) {
                alarmLevel = 2;
            } else {
                alarmLevel = 1;
            }
        } else if (sensor.getAlarmLower() != null && value.compareTo(sensor.getAlarmLower()) < 0) {
            isAlarm = true;
            BigDecimal diff = sensor.getAlarmLower().subtract(value);
            BigDecimal ratio = diff.divide(sensor.getAlarmLower(), 4, BigDecimal.ROUND_HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.5")) >= 0) {
                alarmLevel = 4;
            } else if (ratio.compareTo(new BigDecimal("0.3")) >= 0) {
                alarmLevel = 3;
            } else if (ratio.compareTo(new BigDecimal("0.1")) >= 0) {
                alarmLevel = 2;
            } else {
                alarmLevel = 1;
            }
        }

        reading.setIsAlarm(isAlarm ? 1 : 0);
        reading.setAlarmLevel(alarmLevel);

        sensor.setLastValue(dto.getReadingValue());
        sensor.setLastReadTime(reading.getCollectTime());
        sensorMapper.updateById(sensor);

        if (isAlarm) {
            Alarm alarm = new Alarm();
            alarm.setAlarmCode("ALM" + System.currentTimeMillis());
            alarm.setAlarmType(mapAlarmType(sensor.getSensorType()));
            alarm.setAlarmLevel(alarmLevel);
            alarm.setTitle(getAlarmTitle(sensor.getSensorType()));
            alarm.setContent("传感器" + sensor.getSensorName() + "读数" + value + sensor.getUnit() + "超出阈值范围[" +
                    (sensor.getAlarmLower() != null ? sensor.getAlarmLower() : "-") + "," +
                    (sensor.getAlarmUpper() != null ? sensor.getAlarmUpper() : "-") + "]");
            alarm.setSensorId(sensor.getId());
            alarm.setSensorCode(sensor.getSensorCode());
            alarm.setPipelineId(sensor.getPipelineId());
            alarm.setPipelineCode(sensor.getPipelineCode());
            alarm.setLng(sensor.getLng());
            alarm.setLat(sensor.getLat());
            alarm.setLocation(sensor.getInstallLocation());
            alarm.setAreaCode(sensor.getAreaCode());
            alarm.setAreaName(sensor.getAreaName());
            alarm.setThresholdValue(sensor.getAlarmUpper() != null ? sensor.getAlarmUpper() : sensor.getAlarmLower());
            alarm.setActualValue(value);
            alarm.setUnit(sensor.getUnit());
            alarm.setAlarmTime(reading.getCollectTime());
            alarm.setStatus(1);
            alarmMapper.insert(alarm);
        }

        return this.save(reading);
    }

    private Integer mapAlarmType(Integer sensorType) {
        if (sensorType == null) return 9;
        return switch (sensorType) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 5;
            case 6 -> 6;
            case 7 -> 7;
            default -> 9;
        };
    }

    private String getAlarmTitle(Integer sensorType) {
        if (sensorType == null) return "设备异常告警";
        return switch (sensorType) {
            case 1 -> "压力超限告警";
            case 2 -> "液位超限告警";
            case 3 -> "流量异常告警";
            case 4 -> "温度异常告警";
            case 5 -> "振动异常告警";
            case 6 -> "气体泄漏告警";
            case 7 -> "水质异常告警";
            default -> "设备异常告警";
        };
    }
}
