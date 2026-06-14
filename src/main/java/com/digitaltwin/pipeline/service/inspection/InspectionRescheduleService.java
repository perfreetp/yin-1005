package com.digitaltwin.pipeline.service.inspection;

import com.digitaltwin.pipeline.dto.inspection.DayScheduleVO;
import com.digitaltwin.pipeline.dto.inspection.InsertionResultVO;
import com.digitaltwin.pipeline.dto.inspection.RollbackResultVO;
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

    InsertionResultVO insertTaskWithConflictCheck(InsertionTask task);

    RollbackResultVO rollbackInsertion(Long changeLogId, String operatorName);

    List<InsertionResultVO.ConflictItemVO> checkConflicts(InsertionTask task, Long teamId);

    Boolean syncToCalendar(Long changeLogId, String operatorName);

    DayScheduleVO getDaySchedule(String date, List<Long> teamIds);

    InsertionResultVO applyAlternativePlan(Long insertionTaskId, Integer planIndex, String operatorName);
}
