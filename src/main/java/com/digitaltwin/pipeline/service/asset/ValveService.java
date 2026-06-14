package com.digitaltwin.pipeline.service.asset;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.asset.ValveQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Valve;

import java.util.List;

public interface ValveService extends IService<Valve> {

    PageResult<Valve> selectPage(ValveQueryDTO query);

    Valve selectById(Long id);

    List<Valve> selectByPipelineId(Long pipelineId);

    List<Valve> selectEmergencyValves(Double lng, Double lat, Integer pipelineType, Double radius);
}
