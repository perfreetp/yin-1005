package com.digitaltwin.pipeline.mapper.linkage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.linkage.LinkageNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LinkageNotificationMapper extends BaseMapper<LinkageNotification> {

    @Select("SELECT * FROM linkage_notification WHERE task_id = #{taskId} ORDER BY create_time DESC")
    List<LinkageNotification> selectByTaskId(@Param("taskId") Long taskId);
}
