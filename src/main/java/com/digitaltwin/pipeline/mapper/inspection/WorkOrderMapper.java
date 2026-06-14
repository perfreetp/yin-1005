package com.digitaltwin.pipeline.mapper.inspection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.inspection.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    IPage<WorkOrder> selectPageList(Page<WorkOrder> page,
                                    @Param("orderType") Integer orderType,
                                    @Param("orderSource") Integer orderSource,
                                    @Param("urgency") Integer urgency,
                                    @Param("areaCode") String areaCode,
                                    @Param("status") Integer status,
                                    @Param("undertaker") String undertaker,
                                    @Param("keyword") String keyword);
}
