package com.digitaltwin.pipeline.service.situation;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.situation.PlanExecutionDetailVO;
import com.digitaltwin.pipeline.entity.situation.PlanExecutionRecord;
import com.digitaltwin.pipeline.entity.situation.PlanExecutionTimeline;
import com.digitaltwin.pipeline.entity.situation.ValveOperationRecord;

public interface PlanExecutionService {

    PlanExecutionRecord startExecution(Long alarmId, Long planId, Integer strategyType, String operatorName);

    PlanExecutionDetailVO getExecutionDetail(Long executionId);

    ValveOperationRecord recordValveOperation(Long executionId, ValveOperationRecord record);

    PlanExecutionTimeline addTimelinePoint(Long executionId, PlanExecutionTimeline point);

    PlanExecutionRecord completeExecution(Long executionId, String result, String operatorName);

    PlanExecutionDetailVO generateReplayAnalysis(Long executionId, String reviewerName);

    PageResult<PlanExecutionRecord> executionPage(PageQuery query, Integer status, Integer strategyType, String keyword);

    PlanExecutionDetailVO.ExecutionStuckAnalysisVO getStuckAnalysis(Long executionId);

    PlanExecutionDetailVO confirmValveOperation(Long recordId, String confirmerName, Integer confirmMethod, String remark);

    PlanExecutionDetailVO getFullExecutionRecord(Long executionId);

    byte[] exportExecutionRecord(Long executionId, String exporterName);
}
