package com.digitaltwin.pipeline.mapper.construction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExcavationApplicationMapper extends BaseMapper<ExcavationApplication> {

    IPage<ExcavationApplication> selectPageList(Page<ExcavationApplication> page,
                                                @Param("excavationType") Integer excavationType,
                                                @Param("areaCode") String areaCode,
                                                @Param("status") Integer status,
                                                @Param("keyword") String keyword,
                                                @Param("applicantUnit") String applicantUnit);
}
