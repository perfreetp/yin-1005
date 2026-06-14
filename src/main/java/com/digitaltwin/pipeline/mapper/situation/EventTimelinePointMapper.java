package com.digitaltwin.pipeline.mapper.situation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitaltwin.pipeline.entity.situation.EventTimelinePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EventTimelinePointMapper extends BaseMapper<EventTimelinePoint> {

    @Select("SELECT * FROM event_timeline_point WHERE event_id = #{eventId} ORDER BY occur_time ASC")
    List<EventTimelinePoint> selectByEventId(@Param("eventId") Long eventId);
}
