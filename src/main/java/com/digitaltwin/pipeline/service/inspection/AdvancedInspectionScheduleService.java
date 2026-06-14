package com.digitaltwin.pipeline.service.inspection;

import com.digitaltwin.pipeline.dto.inspection.AdvancedInspectionScheduleDTO;
import com.digitaltwin.pipeline.dto.inspection.SmartInspectionScheduleQueryDTO;

public interface AdvancedInspectionScheduleService {

    AdvancedInspectionScheduleDTO advancedSchedule(SmartInspectionScheduleQueryDTO query);
}
