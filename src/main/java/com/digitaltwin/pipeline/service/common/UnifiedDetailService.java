package com.digitaltwin.pipeline.service.common;

import com.digitaltwin.pipeline.dto.common.BatchQueryItem;
import com.digitaltwin.pipeline.dto.common.UnifiedDetailVO;
import com.digitaltwin.pipeline.dto.common.UnifiedTimelineItemVO;

import java.util.List;

public interface UnifiedDetailService {

    UnifiedDetailVO getDetail(Integer resourceType, Long resourceId);

    List<UnifiedDetailVO> batchGetDetails(List<BatchQueryItem> items);

    List<UnifiedTimelineItemVO> getTimeline(Integer resourceType, Long resourceId, Integer limit);
}
