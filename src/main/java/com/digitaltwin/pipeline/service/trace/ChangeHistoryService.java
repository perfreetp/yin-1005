package com.digitaltwin.pipeline.service.trace;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.trace.ChangeHistoryQueryDTO;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;

import java.util.List;

public interface ChangeHistoryService extends IService<ChangeHistory> {

    PageResult<ChangeHistory> selectPage(ChangeHistoryQueryDTO query);

    List<ChangeHistory> selectByBusiness(Integer businessType, Long businessId);

    void recordChange(Integer businessType, Long businessId, String businessCode,
                      Integer changeType, String fieldName, String oldValue,
                      String newValue, String description, String operator,
                      String operatorDept, String areaCode);
}
