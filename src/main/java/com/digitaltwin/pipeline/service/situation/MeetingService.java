package com.digitaltwin.pipeline.service.situation;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.situation.MeetingDetailVO;
import com.digitaltwin.pipeline.entity.situation.MeetingDecision;
import com.digitaltwin.pipeline.entity.situation.MeetingSession;

import java.util.List;

public interface MeetingService {

    MeetingSession createMeeting(MeetingSession session, List<Long> deptIds, String initiatorName);

    MeetingDetailVO getDetail(Long meetingId);

    PageResult<MeetingSession> page(PageQuery query, Integer meetingType, Integer level, Integer status, String keyword);

    void joinMeeting(Long meetingId, Long userId, String userName, String deptName);

    MeetingDecision addDecision(Long meetingId, MeetingDecision decision, String creatorName);

    void endMeeting(Long meetingId, String conclusion, String operatorName);

    void generateSummary(Long meetingId, String operatorName);

    void updateAttendeeStatus(Long attendeeId, Integer status, String remark);
}
