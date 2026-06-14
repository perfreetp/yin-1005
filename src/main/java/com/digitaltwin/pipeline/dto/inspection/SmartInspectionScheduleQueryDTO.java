package com.digitaltwin.pipeline.dto.inspection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "智能巡检调度查询参数")
public class SmartInspectionScheduleQueryDTO {

    @Schema(description = "调度日期，默认今天")
    private LocalDate scheduleDate;

    @Schema(description = "指定区域编码列表，为空则全区")
    private List<String> areaCodes;

    @Schema(description = "指定管线类型：1-给水 2-排水 3-燃气 4-电力 5-通信 6-热力 7-工业")
    private List<Integer> pipelineTypes;

    @Schema(description = "最低风险等级：1-低 2-中 3-高 4-特高，默认1(全部)")
    private Integer minRiskLevel;

    @Schema(description = "最低紧急度：1-普通 2-较急 3-急 4-特急，默认1(全部)")
    private Integer minUrgency;

    @Schema(description = "巡检组数量，默认自动计算")
    private Integer teamCount;

    @Schema(description = "每组每天最大工时(小时)，默认8")
    private BigDecimal maxHoursPerTeam;

    @Schema(description = "每组每天最大里程(km)，默认15")
    private BigDecimal maxKmPerTeam;

    @Schema(description = "是否包含未完成历史工单，默认true")
    private Boolean includePendingWorkOrders;

    @Schema(description = "是否包含未处理隐患点，默认true")
    private Boolean includeHazards;

    @Schema(description = "是否包含到期常规巡检点，默认true")
    private Boolean includeRoutinePoints;

    @Schema(description = "排班策略：AREA_FIRST-按区域优先 RISK_FIRST-按风险优先 ROUTE_OPTIMAL-路线最优")
    private String scheduleStrategy;
}
