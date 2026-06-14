package com.digitaltwin.pipeline.service.construction;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.construction.ExcavationDTO;
import com.digitaltwin.pipeline.dto.construction.ExcavationQueryDTO;
import com.digitaltwin.pipeline.dto.construction.ExcavationReviewResultDTO;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;

public interface ExcavationApplicationService extends IService<ExcavationApplication> {

    PageResult<ExcavationApplication> selectPage(ExcavationQueryDTO query);

    ExcavationApplication selectById(Long id);

    boolean create(ExcavationDTO dto);

    boolean update(ExcavationDTO dto);

    boolean deleteById(Long id);

    ExcavationReviewResultDTO review(Long applicationId);

    boolean approve(Long applicationId, Integer passed, String opinion, String reviewer);
}
