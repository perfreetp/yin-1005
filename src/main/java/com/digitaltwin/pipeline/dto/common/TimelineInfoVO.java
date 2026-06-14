package com.digitaltwin.pipeline.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一时间信息（所有带创建/更新/截止时间的对象共用）")
public class TimelineInfoVO implements Serializable {

    @Schema(description = "创建/发现时间（ISO格式）")
    private String createTime;

    @Schema(description = "最近更新时间")
    private String updateTime;

    @Schema(description = "截止/预计完成时间")
    private String deadline;

    @Schema(description = "实际开始时间")
    private String actualStartTime;

    @Schema(description = "实际完成时间")
    private String actualFinishTime;

    @Schema(description = "已持续时长描述（如：2小时35分）")
    private String durationText;

    @Schema(description = "剩余时间描述")
    private String remainingText;

    @Schema(description = "是否超时：0-否 1-是")
    private Integer overtime;
}
