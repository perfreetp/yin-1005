package com.digitaltwin.pipeline.mapper.inspection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.inspection.WorkOrderFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkOrderFlowMapper extends BaseMapper<WorkOrderFlow> {

    List<WorkOrderFlow> selectByWorkOrderId(@Param("workOrderId") Long workOrderId);
}
