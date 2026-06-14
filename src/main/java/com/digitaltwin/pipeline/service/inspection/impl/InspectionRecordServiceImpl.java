package com.digitaltwin.pipeline.service.inspection.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.inspection.InspectionRecordQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InspectionRecord;
import com.digitaltwin.pipeline.entity.inspection.InspectionRoute;
import com.digitaltwin.pipeline.mapper.inspection.InspectionRecordMapper;
import com.digitaltwin.pipeline.mapper.inspection.InspectionRouteMapper;
import com.digitaltwin.pipeline.service.inspection.InspectionRecordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InspectionRecordServiceImpl extends ServiceImpl<InspectionRecordMapper, InspectionRecord>
        implements InspectionRecordService {

    private final InspectionRouteMapper routeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<InspectionRecord> selectPage(InspectionRecordQueryDTO query) {
        Page<InspectionRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getRouteId(),
                query.getInspectorId(), query.getAreaCode(), query.getStatus(),
                query.getStartTime(), query.getEndTime()));
    }

    @Override
    public InspectionRecord selectById(Long id) {
        InspectionRecord record = super.getById(id);
        if (record == null) {
            throw new BusinessException("巡检记录不存在");
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionRecord startInspection(Long routeId, Long inspectorId, String inspectorName) {
        InspectionRoute route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException("巡检路线不存在");
        }

        InspectionRecord record = new InspectionRecord();
        record.setRecordCode("IRR" + System.currentTimeMillis());
        record.setRouteId(routeId);
        record.setRouteCode(route.getRouteCode());
        record.setRouteName(route.getRouteName());
        record.setInspectorId(inspectorId);
        record.setInspectorName(inspectorName);
        record.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        record.setStatus(1);
        record.setAreaCode(route.getAreaCode());
        record.setAreaName(route.getAreaName());

        int totalPoints = 0;
        try {
            if (route.getCheckPoints() != null) {
                JsonNode nodes = objectMapper.readTree(route.getCheckPoints());
                totalPoints = nodes.size();
            }
        } catch (Exception ignored) {
        }
        record.setTotalPoints(totalPoints);
        record.setCheckedPoints(0);
        record.setCompletionRate(0);
        record.setDefectCount(0);
        record.setReportedDefectCount(0);

        this.save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionRecord endInspection(Long recordId, String trajectory, Integer checkedPoints,
                                          BigDecimal actualDistance, Integer defectCount, Integer reportedDefectCount) {
        InspectionRecord record = super.getById(recordId);
        if (record == null) {
            throw new BusinessException("巡检记录不存在");
        }

        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        record.setEndTime(endTime);
        record.setTrajectory(trajectory);

        try {
            LocalDateTime start = LocalDateTime.parse(record.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            record.setDuration((int) Duration.between(start, end).toMinutes());
        } catch (Exception ignored) {
        }

        record.setActualDistance(actualDistance);
        if (checkedPoints != null) {
            record.setCheckedPoints(checkedPoints);
        }
        if (record.getTotalPoints() != null && record.getTotalPoints() > 0) {
            int rate = record.getCheckedPoints() * 100 / record.getTotalPoints();
            record.setCompletionRate(Math.min(rate, 100));
        }
        if (defectCount != null) {
            record.setDefectCount(defectCount);
        }
        if (reportedDefectCount != null) {
            record.setReportedDefectCount(reportedDefectCount);
        }
        record.setStatus(2);

        this.updateById(record);
        return record;
    }
}
