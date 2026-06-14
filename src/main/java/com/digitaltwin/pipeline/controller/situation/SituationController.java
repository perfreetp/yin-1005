package com.digitaltwin.pipeline.controller.situation;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.situation.EventReplayVO;
import com.digitaltwin.pipeline.dto.situation.SituationSnapshotVO;
import com.digitaltwin.pipeline.entity.situation.EventIncident;
import com.digitaltwin.pipeline.service.situation.SituationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "事件态势与历史回放")
@RestController
@RequestMapping("/situation")
@RequiredArgsConstructor
public class SituationController {

    private final SituationService situationService;

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
}
