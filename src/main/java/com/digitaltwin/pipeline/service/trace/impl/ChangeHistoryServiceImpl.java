package com.digitaltwin.pipeline.service.trace.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.trace.ChangeHistoryQueryDTO;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;
import com.digitaltwin.pipeline.mapper.trace.ChangeHistoryMapper;
import com.digitaltwin.pipeline.service.trace.ChangeHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChangeHistoryServiceImpl extends ServiceImpl<ChangeHistoryMapper, ChangeHistory>
        implements ChangeHistoryService {

    @Override
    public PageResult<ChangeHistory> selectPage(ChangeHistoryQueryDTO query) {
        Page<ChangeHistory> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getBusinessType(),
                query.getBusinessId(), query.getChangeType(), query.getAreaCode(),
                query.getOperator(), query.getStartTime(), query.getEndTime()));
    }

    @Override
    public List<ChangeHistory> selectByBusiness(Integer businessType, Long businessId) {
        return this.lambdaQuery()
                .eq(businessType != null, ChangeHistory::getBusinessType, businessType)
                .eq(businessId != null, ChangeHistory::getBusinessId, businessId)
                .orderByDesc(ChangeHistory::getOperateTime)
                .list();
    }

    @Override
    public void recordChange(Integer businessType, Long businessId, String businessCode,
                             Integer changeType, String fieldName, String oldValue,
                             String newValue, String description, String operator,
                             String operatorDept, String areaCode) {
        ChangeHistory history = new ChangeHistory();
        history.setBusinessType(businessType);
        history.setBusinessId(businessId);
        history.setBusinessCode(businessCode);
        history.setChangeType(changeType);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setDescription(description);
        history.setOperator(operator);
        history.setOperatorDept(operatorDept);
        history.setOperateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        history.setAreaCode(areaCode);
        this.save(history);
    }
}
