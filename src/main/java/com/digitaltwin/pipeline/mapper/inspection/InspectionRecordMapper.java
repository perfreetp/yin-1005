package com.digitaltwin.pipeline.mapper.inspection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.inspection.InspectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InspectionRecordMapper extends BaseMapper<InspectionRecord> {

    IPage<InspectionRecord> selectPageList(Page<InspectionRecord> page,
                                           @Param("routeId") Long routeId,
                                           @Param("inspectorId") Long inspectorId,
                                           @Param("areaCode") String areaCode,
                                           @Param("status") Integer status,
                                           @Param("startTime") String startTime,
                                           @Param("endTime") String endTime);
}
