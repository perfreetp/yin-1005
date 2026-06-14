package com.digitaltwin.pipeline.controller.situation;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.situation.MeetingDetailVO;
import com.digitaltwin.pipeline.entity.situation.MeetingDecision;
import com.digitaltwin.pipeline.entity.situation.MeetingDecisionTrace;
import com.digitaltwin.pipeline.entity.situation.MeetingSession;
import com.digitaltwin.pipeline.entity.situation.MeetingSummary;
import com.digitaltwin.pipeline.service.situation.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "跨部门会商")
@RestController
@RequestMapping("/situation/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @Operation(summary = "创建会商会议")
    @PostMapping("/create")
    public Result<MeetingSession> create(@RequestBody MeetingSession session,
                                         @RequestParam(required = false) List<Long> deptIds,
                                         @RequestParam(required = false) String initiatorName) {
        return Result.success(meetingService.createMeeting(session, deptIds, initiatorName));
    }

    @Operation(summary = "获取会议详情")
    @GetMapping("/{id}")
    public Result<MeetingDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(meetingService.getDetail(id));
    }

    @Operation(summary = "分页查询会议列表")
    @GetMapping("/page")
    public Result<PageResult<MeetingSession>> page(PageQuery query,
                                                   @RequestParam(required = false) Integer meetingType,
                                                   @RequestParam(required = false) Integer level,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String keyword) {
        return Result.success(meetingService.page(query, meetingType, level, status, keyword));
    }

    @Operation(summary = "加入会议")
    @PostMapping("/{id}/join")
    public Result<Void> join(@PathVariable Long id,
                             @RequestParam(required = false) Long userId,
                             @RequestParam String userName,
                             @RequestParam(required = false) String deptName) {
        meetingService.joinMeeting(id, userId, userName, deptName);
        return Result.success();
    }

    @Operation(summary = "新增会议决策")
    @PostMapping("/{id}/decision")
    public Result<MeetingDecision> addDecision(@PathVariable Long id,
                                               @RequestBody MeetingDecision decision,
                                               @RequestParam(required = false) String creatorName) {
        return Result.success(meetingService.addDecision(id, decision, creatorName));
    }

    @Operation(summary = "结束会议")
    @PostMapping("/{id}/end")
    public Result<Void> end(@PathVariable Long id,
                            @RequestParam(required = false) String conclusion,
                            @RequestParam(required = false) String operatorName) {
        meetingService.endMeeting(id, conclusion, operatorName);
        return Result.success();
    }

    @Operation(summary = "生成会议纪要")
    @PostMapping("/{id}/summary")
    public Result<Void> generateSummary(@PathVariable Long id,
                                        @RequestParam(required = false) String operatorName) {
        meetingService.generateSummary(id, operatorName);
        return Result.success();
    }

    @Operation(summary = "更新参会人状态")
    @PutMapping("/attendee/{id}/status")
    public Result<Void> updateAttendeeStatus(@PathVariable Long id,
                                             @RequestParam Integer status,
                                             @RequestParam(required = false) String remark) {
        meetingService.updateAttendeeStatus(id, status, remark);
        return Result.success();
    }

    @Operation(summary = "更新决策")
    @PutMapping("/decision/{id}")
    public Result<MeetingDecision> updateDecision(@PathVariable Long id,
                                                  @RequestBody MeetingDecision decision,
                                                  @RequestParam(required = false) String operatorName) {
        return Result.success(meetingService.updateDecision(id, decision, operatorName));
    }

    @Operation(summary = "更新决策进度")
    @PutMapping("/decision/{id}/progress")
    public Result<Void> updateDecisionProgress(@PathVariable Long id,
                                               @RequestParam(required = false) Integer progress,
                                               @RequestParam(required = false) String result,
                                               @RequestParam(required = false) String operatorName) {
        meetingService.updateDecisionProgress(id, progress, result, operatorName);
        return Result.success();
    }

    @Operation(summary = "验证确认决策")
    @PostMapping("/decision/{id}/verify")
    public Result<Void> verifyDecision(@PathVariable Long id,
                                       @RequestParam(required = false) String verificationRemark,
                                       @RequestParam String verificationPerson) {
        meetingService.verifyDecision(id, verificationRemark, verificationPerson);
        return Result.success();
    }

    @Operation(summary = "否决决策")
    @PostMapping("/decision/{id}/veto")
    public Result<Void> vetoDecision(@PathVariable Long id,
                                     @RequestParam(required = false) String remark,
                                     @RequestParam(required = false) String operatorName) {
        meetingService.vetoDecision(id, remark, operatorName);
        return Result.success();
    }

    @Operation(summary = "编辑纪要")
    @PutMapping("/summary/{meetingId}")
    public Result<MeetingSummary> editSummary(@PathVariable Long meetingId,
                                              @RequestBody MeetingSummary summary,
                                              @RequestParam(required = false) String editorName) {
        return Result.success(meetingService.editSummary(meetingId, summary, editorName));
    }

    @Operation(summary = "决议追踪时间线")
    @GetMapping("/decision/{id}/traces")
    public Result<List<MeetingDecisionTrace>> getDecisionTraces(@PathVariable Long id) {
        return Result.success(meetingService.getDecisionTraces(id));
    }

    @Operation(summary = "决议落地总览")
    @GetMapping("/{id}/decision-tracking")
    public Result<MeetingDetailVO> getDecisionTracking(@PathVariable Long id) {
        return Result.success(meetingService.getDecisionTrackingView(id));
    }
}
