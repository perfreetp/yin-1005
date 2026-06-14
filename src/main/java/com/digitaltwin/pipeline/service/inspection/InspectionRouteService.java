package com.digitaltwin.pipeline.service.inspection;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.inspection.InspectionRouteQueryDTO;
import com.digitaltwin.pipeline.entity.inspection.InspectionRoute;

import java.util.List;

public interface InspectionRouteService extends IService<InspectionRoute> {

    PageResult<InspectionRoute> selectPage(InspectionRouteQueryDTO query);

    InspectionRoute selectById(Long id);

    InspectionRoute generateRoute(String areaCode, Integer pipelineType, Integer routeType);
}
