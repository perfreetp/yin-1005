package com.digitaltwin.pipeline.controller.situation;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.situation.EventReplayVO;
import com.digitaltwin.pipeline.dto.situation.PlanExecutionDetailVO;
import com.digitaltwin.pipeline.dto.situation.SituationSnapshotVO;
import com.digitaltwin.pipeline.entity.situation.EventIncident;
import com.digitaltwin.pipeline.entity.situation.PlanExecutionRecord;
import com.digitaltwin.pipeline.entity.situation.PlanExecutionTimeline;
import com.digitaltwin.pipeline.entity.situation.ValveOperationRecord;
import com.digitaltwin.pipeline.service.situation.PlanExecutionService;
import com.digitaltwin.pipeline.service.situation.SituationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "事件态势与历史回放")
@RestController
@RequestMapping("/situation")
@RequiredArgsConstructor
public class SituationController {

    private final SituationService situationService;
    private final PlanExecutionService planExecutionService;

    @Operation(summary = "一张图态势总览（当前实时快照：告警/开挖/事件/工单/异常阀门/热力图/24h预测）")
    @GetMapping("/snapshot")
    public Result<SituationSnapshotVO> snapshot(@RequestParam(required = false) String areaCode) {
        return Result.success(situationService.getCurrentSnapshot(areaCode));
    }

    @Operation(summary = "历史事件回放（含时间轴拖动、各时刻资产状态快照、处置总结）")
    @GetMapping("/event/{id}/replay")
    public Result<EventReplayVO> replay(@PathVariable Long id) {
        return Result.success(situationService.getEventReplay(id));
    }

    @Operation(summary = "分页查询历史事件")
    @GetMapping("/event/page")
    public Result<PageResult<EventIncident>> eventPage(PageQuery query,
                                                       @RequestParam(required = false) Integer eventType,
                                                       @RequestParam(required = false) Integer eventLevel,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) String areaCode) {
        return Result.success(situationService.selectIncidentPage(query, eventType, eventLevel, status, areaCode));
    }

    @Operation(summary = "【方案执行】获取执行详情")
    @GetMapping("/execution/{id}")
    public Result<PlanExecutionDetailVO> getExecutionDetail(@PathVariable Long id) {
        return Result.success(planExecutionService.getExecutionDetail(id));
    }

    @Operation(summary = "【方案执行】记录阀门操作")
    @PostMapping("/execution/{id}/valve-op")
    public Result<ValveOperationRecord> recordValveOperation(
            @PathVariable Long id,
            @RequestBody ValveOperationRecord record) {
        return Result.success(planExecutionService.recordValveOperation(id, record));
    }

    @Operation(summary = "【方案执行】追加时间轴节点")
    @PostMapping("/execution/{id}/timeline")
    public Result<PlanExecutionTimeline> addTimelinePoint(
            @PathVariable Long id,
            @RequestBody PlanExecutionTimeline point) {
        return Result.success(planExecutionService.addTimelinePoint(id, point));
    }

    @Operation(summary = "【方案执行】完成执行")
    @PostMapping("/execution/{id}/complete")
    public Result<PlanExecutionRecord> completeExecution(
            @PathVariable Long id,
            @RequestParam String result,
            @RequestParam String operatorName) {
        return Result.success(planExecutionService.completeExecution(id, result, operatorName));
    }

    @Operation(summary = "【方案执行】生成复盘分析")
    @PostMapping("/execution/{id}/replay")
    public Result<PlanExecutionDetailVO> generateReplayAnalysis(
            @PathVariable Long id,
            @RequestParam String reviewerName) {
        return Result.success(planExecutionService.generateReplayAnalysis(id, reviewerName));
    }

    @Operation(summary = "【方案执行】分页查询执行记录")
    @GetMapping("/execution/page")
    public Result<PageResult<PlanExecutionRecord>> executionPage(PageQuery query,
                                                                  @RequestParam(required = false) Integer status,
                                                                  @RequestParam(required = false) Integer strategyType,
                                                                  @RequestParam(required = false) String keyword) {
        return Result.success(planExecutionService.executionPage(query, status, strategyType, keyword));
    }

    @Operation(summary = "【方案执行】获取超时卡点分析")
    @GetMapping("/execution/{id}/stuck-analysis")
    public Result<PlanExecutionDetailVO.ExecutionStuckAnalysisVO> getStuckAnalysis(@PathVariable Long id) {
        return Result.success(planExecutionService.getStuckAnalysis(id));
    }

    @Operation(summary = "【方案执行】确认阀门操作")
    @PostMapping("/execution/valve-op/{recordId}/confirm")
    public Result<PlanExecutionDetailVO> confirmValveOperation(
            @PathVariable Long recordId,
            @RequestParam String confirmerName,
            @RequestParam Integer confirmMethod,
            @RequestParam(required = false) String remark) {
        return Result.success(planExecutionService.confirmValveOperation(recordId, confirmerName, confirmMethod, remark));
    }

    @Operation(summary = "【方案执行】获取完整执行记录（导出用）")
    @GetMapping("/execution/{id}/full")
    public Result<PlanExecutionDetailVO> getFullExecutionRecord(@PathVariable Long id) {
        return Result.success(planExecutionService.getFullExecutionRecord(id));
    }

    @Operation(summary = "【方案执行】导出执行记录")
    @GetMapping("/execution/{id}/export")
    public ResponseEntity<byte[]> exportExecutionRecord(
            @PathVariable Long id,
            @RequestParam String exporterName) {
        byte[] data = planExecutionService.exportExecutionRecord(id, exporterName);
        String fileName = "execution-record-" + id + ".txt";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
