package com.digitaltwin.pipeline.service.situation;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.situation.EventReplayVO;
import com.digitaltwin.pipeline.dto.situation.SituationSnapshotVO;
import com.digitaltwin.pipeline.entity.situation.EventIncident;

public interface SituationService {

    SituationSnapshotVO getCurrentSnapshot(String areaCode);

    EventReplayVO getEventReplay(Long eventId);

    PageResult<EventIncident> selectIncidentPage(PageQuery query, Integer eventType, Integer eventLevel,
                                                Integer status, String areaCode);
}
