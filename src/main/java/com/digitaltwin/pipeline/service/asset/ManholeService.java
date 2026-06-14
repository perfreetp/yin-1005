package com.digitaltwin.pipeline.service.asset;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.asset.ManholeQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;

import java.util.List;

public interface ManholeService extends IService<Manhole> {

    PageResult<Manhole> selectPage(ManholeQueryDTO query);

    Manhole selectById(Long id);

    List<Manhole> selectByAreaAndType(String areaCode, Integer manholeType);
}
