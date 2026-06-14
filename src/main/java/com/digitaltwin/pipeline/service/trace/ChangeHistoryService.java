package com.digitaltwin.pipeline.service.trace;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.trace.ChangeTimelineDTO;
import com.digitaltwin.pipeline.dto.trace.ChangeHistoryQueryDTO;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;

import java.util.List;

public interface ChangeHistoryService {

    PageResult<ChangeHistory> selectPage(ChangeHistoryQueryDTO query);

    ChangeTimelineDTO getResourceTimeline(Integer resourceType, Long resourceId);

    void recordChange(Integer resourceType, Long resourceId, String resourceName,
                      Integer operationType, String operation,
                      String beforeValue, String afterValue,
                      Long operatorId, String operatorName, String ipAddress,
                      String remark);
}
