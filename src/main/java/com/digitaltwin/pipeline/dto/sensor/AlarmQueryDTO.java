package com.digitaltwin.pipeline.dto.sensor;

import com.digitaltwin.pipeline.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "告警查询DTO")
public class AlarmQueryDTO extends PageQuery {

    @Schema(description = "告警类型")
    private Integer alarmType;

    @Schema(description = "告警级别")
    private Integer alarmLevel;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
