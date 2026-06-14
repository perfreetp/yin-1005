package com.digitaltwin.pipeline.dto.inspection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "回滚结果VO")
public class RollbackResultVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "回滚日志ID")
    private Long rollbackLogId;

    @Schema(description = "消息")
    private String message;

    @Schema(description = "恢复的任务数")
    private Integer restoredTaskCount;

    @Schema(description = "受影响班组数")
    private Integer affectedTeamCount;
}
