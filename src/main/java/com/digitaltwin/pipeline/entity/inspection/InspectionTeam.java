package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_team")
@Schema(description = "巡检班组")
public class InspectionTeam extends BaseEntity {

    @Schema(description = "班组编码")
    private String teamCode;

    @Schema(description = "班组名称")
    private String teamName;

    @Schema(description = "班长姓名")
    private String leaderName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "负责区域编码")
    private String areaCode;

    @Schema(description = "负责区域名称")
    private String areaName;

    @Schema(description = "成员数量")
    private Integer memberCount;

    @Schema(description = "每日最大工时(小时)，默认8")
    private BigDecimal maxDailyHours;

    @Schema(description = "每日最大里程(km)，默认15")
    private BigDecimal maxDailyKm;

    @Schema(description = "上班时间，格式HH:mm，默认08:30")
    private String workStartTime;

    @Schema(description = "下班时间，格式HH:mm，默认17:30")
    private String workEndTime;

    @Schema(description = "巡检能力，逗号分隔，如GAS,WATER,POWER")
    private String capabilities;

    @Schema(description = "状态：1-在岗 2-出勤 3-休息")
    private Integer status;
}
