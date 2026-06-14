package com.digitaltwin.pipeline.mapper.inspection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.inspection.Inspector;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InspectorMapper extends BaseMapper<Inspector> {
}
