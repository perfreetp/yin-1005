package com.digitaltwin.pipeline.service.inspection;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.inspection.DefectQueryDTO;
import com.digitaltwin.pipeline.dto.inspection.DefectReportDTO;
import com.digitaltwin.pipeline.dto.inspection.InspectionRecordQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.DefectReport;
import com.digitaltwin.pipeline.entity.inspection.InspectionRecord;

public interface DefectReportService extends IService<DefectReport> {

    PageResult<DefectReport> selectPage(DefectQueryDTO query);

    DefectReport selectById(Long id);

    DefectReport report(DefectReportDTO dto);

    boolean receive(Long id, String receiver);
}
