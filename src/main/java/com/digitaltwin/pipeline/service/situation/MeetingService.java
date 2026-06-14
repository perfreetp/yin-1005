package com.digitaltwin.pipeline.service.situation;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.situation.MeetingDetailVO;
import com.digitaltwin.pipeline.entity.situation.MeetingDecision;
import com.digitaltwin.pipeline.entity.situation.MeetingDecisionTrace;
import com.digitaltwin.pipeline.entity.situation.MeetingSession;
import com.digitaltwin.pipeline.entity.situation.MeetingSummary;

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

    MeetingDecision updateDecision(Long decisionId, MeetingDecision decision, String operatorName);

    void updateDecisionProgress(Long decisionId, Integer progress, String result, String operatorName);

    void verifyDecision(Long decisionId, String verificationRemark, String verificationPerson);

    void vetoDecision(Long decisionId, String remark, String operatorName);

    MeetingSummary editSummary(Long meetingId, MeetingSummary summary, String editorName);

    List<MeetingDecisionTrace> getDecisionTraces(Long decisionId);

    MeetingDetailVO getDecisionTrackingView(Long meetingId);
}
