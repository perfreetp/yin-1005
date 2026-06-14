package com.digitaltwin.pipeline.service.sensor;

import com.digitaltwin.pipeline.dto.situation.EmergencyPlanComparisonDTO;

public interface EmergencyPlanService {

    EmergencyPlanComparisonDTO comparePlans(Long alarmId);

    EmergencyPlanComparisonDTO.DisposalPlanVO getSinglePlan(Long alarmId, Integer strategyType);

    EmergencyPlanComparisonDTO selectAndExecute(Long alarmId, Long planId, String operatorName);
}
