package com.digitaltwin.pipeline.mapper.asset;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManholeMapper extends BaseMapper<Manhole> {

    IPage<Manhole> selectPageList(Page<Manhole> page,
                                  @Param("manholeType") Integer manholeType,
                                  @Param("areaCode") String areaCode,
                                  @Param("status") Integer status,
                                  @Param("keyword") String keyword);

    List<Manhole> selectByAreaAndType(@Param("areaCode") String areaCode,
                                      @Param("manholeType") Integer manholeType);
}
