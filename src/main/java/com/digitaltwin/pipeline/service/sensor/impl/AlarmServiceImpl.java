package com.digitaltwin.pipeline.service.sensor.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.sensor.AlarmQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Alarm;
import com.digitaltwin.pipeline.mapper.sensor.AlarmMapper;
import com.digitaltwin.pipeline.service.sensor.AlarmService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AlarmServiceImpl extends ServiceImpl<AlarmMapper, Alarm> implements AlarmService {

    @Override
    public PageResult<Alarm> selectPage(AlarmQueryDTO query) {
        Page<Alarm> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getAlarmType(),
                query.getAlarmLevel(), query.getAreaCode(), query.getStatus(),
                query.getStartTime(), query.getEndTime()));
    }

    @Override
    public Alarm selectById(Long id) {
        Alarm alarm = super.getById(id);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }
        return alarm;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlarm(Long id, String handler, String handleResult) {
        Alarm alarm = super.getById(id);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }
        alarm.setStatus(3);
        alarm.setHandler(handler);
        alarm.setHandleTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        alarm.setHandleResult(handleResult);
        return this.updateById(alarm);
    }
}
