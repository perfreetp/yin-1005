package com.digitaltwin.pipeline.service.inspection;

import com.digitaltwin.pipeline.dto.inspection.InsertionResultVO;
import com.digitaltwin.pipeline.entity.inspection.InsertionTask;
import com.digitaltwin.pipeline.entity.inspection.ScheduleChangeLog;

import java.util.List;

public interface InspectionRescheduleService {

    InsertionResultVO insertTask(InsertionTask task);

    ScheduleChangeLog cancelTask(Long taskId, String reason, String operatorName);

    ScheduleChangeLog adjustTaskPerson(Long taskId, Long newPersonId, String reason, String operatorName);

    ScheduleChangeLog adjustTaskVehicle(Long taskId, Long newVehicleId, String reason, String operatorName);

    List<ScheduleChangeLog> getDayChangeLogs(String date);

    InsertionResultVO simulateInsertion(InsertionTask task);
}
