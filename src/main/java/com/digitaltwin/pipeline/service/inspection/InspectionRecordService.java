package com.digitaltwin.pipeline.service.inspection;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.inspection.InspectionRecordQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InspectionRecord;

public interface InspectionRecordService extends IService<InspectionRecord> {

    PageResult<InspectionRecord> selectPage(InspectionRecordQueryDTO query);

    InspectionRecord selectById(Long id);

    InspectionRecord startInspection(Long routeId, Long inspectorId, String inspectorName);

    InspectionRecord endInspection(Long recordId, String trajectory, Integer checkedPoints,
                                   java.math.BigDecimal actualDistance, Integer defectCount, Integer reportedDefectCount);
}
