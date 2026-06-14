package com.digitaltwin.pipeline.service.sensor;

import com.digitaltwin.pipeline.dto.sensor.AlarmDisposalSuggestionDTO;

import java.util.List;

public interface AlarmDisposalService {

    AlarmDisposalSuggestionDTO getDisposalSuggestion(Long alarmId);

    List<AlarmDisposalSuggestionDTO> getPendingAlarmPriorityList();
}
