package com.digitaltwin.pipeline.mapper.trace;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChangeHistoryMapper extends BaseMapper<ChangeHistory> {

    IPage<ChangeHistory> selectPageList(Page<ChangeHistory> page,
                                        @Param("businessType") Integer businessType,
                                        @Param("businessId") Long businessId,
                                        @Param("changeType") Integer changeType,
                                        @Param("areaCode") String areaCode,
                                        @Param("operator") String operator,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime);
}
