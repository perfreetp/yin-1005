package com.digitaltwin.pipeline.controller.linkage;

import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.linkage.LinkedTaskDetailVO;
import com.digitaltwin.pipeline.dto.linkage.LinkedTaskTriggerDTO;
import com.digitaltwin.pipeline.entity.linkage.LinkedTask;
import com.digitaltwin.pipeline.entity.linkage.LinkageNotification;
import com.digitaltwin.pipeline.service.linkage.LinkedTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "联动任务流引擎（一张图×审批×巡检 三端串联）")
@RestController
@RequestMapping("/linkage/task")
@RequiredArgsConstructor
public class LinkedTaskController {

    private final LinkedTaskService linkedTaskService;

    @Operation(summary = "从来源业务触发联动任务（审批/告警/隐患 → 自动转待办+通知+工单）")
    @PostMapping("/trigger")
    public Result<LinkedTask> trigger(@RequestBody LinkedTaskTriggerDTO dto) {
        return Result.success(linkedTaskService.triggerFromSource(dto));
    }

    @Operation(summary = "联动任务详情（含完整溯源链：来源→通知→流转→子工单→当前进展）")
    @GetMapping("/{id}/detail")
    public Result<LinkedTaskDetailVO> detail(@PathVariable Long id) {
        return Result.success(linkedTaskService.getDetail(id));
    }

    @Operation(summary = "分页查询联动任务")
    @GetMapping("/page")
    public Result<PageResult<LinkedTask>> page(PageQuery query,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) Integer priority,
                                               @RequestParam(required = false) Integer taskType,
                                               @RequestParam(required = false) Integer sourceType,
                                               @RequestParam(required = false) String areaCode) {
        return Result.success(linkedTaskService.selectPage(query, status, priority, taskType, sourceType, areaCode));
    }

    @Operation(summary = "查看任务的所有通知记录")
    @GetMapping("/{id}/notifications")
    public Result<List<LinkageNotification>> notifications(@PathVariable Long id) {
        return Result.success(linkedTaskService.getTaskNotifications(id));
    }

    @Operation(summary = "派单")
    @PostMapping("/{id}/assign")
    public Result<LinkedTask> assign(@PathVariable Long id,
                                     @RequestParam Long deptId,
                                     @RequestParam(required = false) Long userId,
                                     @RequestParam String operatorName) {
        return Result.success(linkedTaskService.assignTask(id, deptId, userId, operatorName));
    }

    @Operation(summary = "上报进度（子工单同步更新）")
    @PostMapping("/{id}/progress")
    public Result<LinkedTask> progress(@PathVariable Long id,
                                       @RequestParam Integer progress,
                                       @RequestParam(required = false) String content,
                                       @RequestParam String operatorName) {
        return Result.success(linkedTaskService.reportProgress(id, progress, content, operatorName));
    }

    @Operation(summary = "完成任务")
    @PostMapping("/{id}/complete")
    public Result<LinkedTask> complete(@PathVariable Long id,
                                       @RequestParam(required = false) String result,
                                       @RequestParam String operatorName) {
        return Result.success(linkedTaskService.completeTask(id, result, operatorName));
    }

    @Operation(summary = "手动追加通知")
    @PostMapping("/{id}/notify")
    public Result<Void> notify(@PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "2") Integer notifyType,
                               @RequestParam(required = false) Long receiverId,
                               @RequestParam String receiverName,
                               @RequestParam String title,
                               @RequestParam String content,
                               @RequestParam(required = false, defaultValue = "2") Integer level) {
        linkedTaskService.sendNotification(id, notifyType, receiverId, receiverName, title, content, level);
        return Result.success();
    }
}
