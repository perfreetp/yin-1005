package com.digitaltwin.pipeline.service.sensor;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.sensor.HazardDTO;
import com.digitaltwin.pipeline.dto.sensor.HazardQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Hazard;

public interface HazardService extends IService<Hazard> {

    PageResult<Hazard> selectPage(HazardQueryDTO query);

    Hazard selectById(Long id);

    boolean create(HazardDTO dto);

    boolean update(HazardDTO dto);

    boolean deleteById(Long id);

    int calculateRiskScore(Hazard hazard);
}
