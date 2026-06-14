package com.digitaltwin.pipeline.mapper.linkage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.linkage.LinkedTaskFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LinkedTaskFlowMapper extends BaseMapper<LinkedTaskFlow> {

    @Select("SELECT * FROM linked_task_flow WHERE task_id = #{taskId} ORDER BY create_time ASC")
    List<LinkedTaskFlow> selectByTaskId(@Param("taskId") Long taskId);
}
