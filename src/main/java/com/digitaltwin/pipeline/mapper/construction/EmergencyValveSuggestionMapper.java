package com.digitaltwin.pipeline.mapper.construction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.construction.EmergencyValveSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmergencyValveSuggestionMapper extends BaseMapper<EmergencyValveSuggestion> {

    List<EmergencyValveSuggestion> selectByApplicationId(@Param("applicationId") Long applicationId);
}
