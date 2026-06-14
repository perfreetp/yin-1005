package com.digitaltwin.pipeline.controller.common;

import com.digitaltwin.pipeline.common.Result;
import com.digitaltwin.pipeline.dto.common.CommonEnumsSnapshotVO;
import com.digitaltwin.pipeline.dto.common.EnumValueVO;
import com.digitaltwin.pipeline.enums.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "通用枚举与口径（三端联调用）")
@RestController
@RequestMapping("/common/enums")
public class CommonEnumController {

    @Operation(summary = "拉取所有通用枚举的统一快照（一张图/审批/巡检三端共用）")
    @GetMapping("/snapshot")
    public Result<CommonEnumsSnapshotVO> snapshot() {
        return Result.success(CommonEnumsSnapshotVO.builder()
                .pipelineTypes(toVO(PipelineTypeEnum.values()))
                .assetStatuses(toVOWithColor(AssetStatusEnum.values()))
                .priorityLevels(toVOWithColor(PriorityLevelEnum.values()))
                .alarmTypes(toVO(AlarmTypeEnum.values()))
                .alarmStatuses(toVO(AlarmStatusEnum.values()))
                .workOrderStatuses(toVO(WorkOrderStatusEnum.values()))
                .workOrderSources(toVO(WorkOrderSourceEnum.values()))
                .excavationStatuses(toVO(ExcavationStatusEnum.values()))
                .resourceTypes(toVO(ResourceTypeEnum.values()))
                .operationTypes(toVO(OperationTypeEnum.values()))
                .eventLevels(toVOWithColor(EventLevelEnum.values()))
                .build());
    }

    @Operation(summary = "管线类型枚举")
    @GetMapping("/pipeline-types")
    public Result<List<EnumValueVO>> pipelineTypes() {
        return Result.success(toVO(PipelineTypeEnum.values()));
    }

    @Operation(summary = "优先级枚举")
    @GetMapping("/priorities")
    public Result<List<EnumValueVO>> priorities() {
        return Result.success(toVOWithColor(PriorityLevelEnum.values()));
    }

    @Operation(summary = "事件级别枚举")
    @GetMapping("/event-levels")
    public Result<List<EnumValueVO>> eventLevels() {
        return Result.success(toVOWithColor(EventLevelEnum.values()));
    }

    private List<EnumValueVO> toVO(Object[] enums) {
        return Arrays.stream(enums).map(e -> {
            try {
                Integer code = (Integer) e.getClass().getMethod("getCode").invoke(e);
                String key = (String) e.getClass().getMethod("getKey").invoke(e);
                String label = (String) e.getClass().getMethod("getLabel").invoke(e);
                return EnumValueVO.builder().code(code).key(key).label(label).build();
            } catch (Exception ex) {
                return EnumValueVO.builder().label("未知").build();
            }
        }).collect(Collectors.toList());
    }

    private List<EnumValueVO> toVOWithColor(Object[] enums) {
        return Arrays.stream(enums).map(e -> {
            try {
                Integer code = (Integer) e.getClass().getMethod("getCode").invoke(e);
                String key = (String) e.getClass().getMethod("getKey").invoke(e);
                String label = (String) e.getClass().getMethod("getLabel").invoke(e);
                String color = null;
                try {
                    color = (String) e.getClass().getMethod("getColor").invoke(e);
                } catch (Exception ignored) { }
                return EnumValueVO.builder().code(code).key(key).label(label).color(color).build();
            } catch (Exception ex) {
                return EnumValueVO.builder().label("未知").build();
            }
        }).collect(Collectors.toList());
    }
}
