package com.digitaltwin.pipeline.mapper.inspection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.inspection.DefectReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DefectReportMapper extends BaseMapper<DefectReport> {

    IPage<DefectReport> selectPageList(Page<DefectReport> page,
                                       @Param("defectType") Integer defectType,
                                       @Param("defectLevel") Integer defectLevel,
                                       @Param("areaCode") String areaCode,
                                       @Param("status") Integer status,
                                       @Param("reportSource") Integer reportSource,
                                       @Param("keyword") String keyword);
}
