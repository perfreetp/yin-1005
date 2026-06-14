package com.digitaltwin.pipeline.service.inspection;

import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleQueryDTO;

public interface SmartInspectionScheduleService {

    SmartInspectionScheduleDTO generateSchedule(SmartInspectionScheduleQueryDTO query);
}
