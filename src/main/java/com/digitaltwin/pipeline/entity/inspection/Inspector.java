package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspector")
@Schema(description = "巡检人员")
public class Inspector extends BaseEntity {

    @Schema(description = "人员编号")
    private String inspectorCode;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "所属班组ID")
    private Long teamId;

    @Schema(description = "所属班组名称")
    private String teamName;

    @Schema(description = "技能，逗号分隔")
    private String skills;

    @Schema(description = "工作年限")
    private Integer experienceYears;

    @Schema(description = "工作状态：1-空闲 2-在途 3-作业中")
    private Integer workStatus;

    @Schema(description = "当前经度")
    private BigDecimal currentLng;

    @Schema(description = "当前纬度")
    private BigDecimal currentLat;

    @Schema(description = "今日已工作工时(小时)")
    private BigDecimal dailyWorkedHours;
}
