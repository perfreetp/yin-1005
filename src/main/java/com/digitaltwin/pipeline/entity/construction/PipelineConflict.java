package com.digitaltwin.pipeline.entity.construction;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_conflict")
@Schema(description = "管线冲突记录")
public class PipelineConflict extends BaseEntity {

    @Schema(description = "冲突编号")
    private String conflictCode;

    @Schema(description = "关联开挖申请ID")
    private Long applicationId;

    @Schema(description = "关联开挖申请编号")
    private String applicationCode;

    @Schema(description = "管线ID")
    private Long pipelineId;

    @Schema(description = "管线编号")
    private String pipelineCode;

    @Schema(description = "管线名称")
    private String pipelineName;

    @Schema(description = "管线类型：1-给水 2-排水 3-燃气 4-电力 5-通信 6-热力 7-工业")
    private Integer pipelineType;

    @Schema(description = "冲突类型：1-水平净距不足 2-垂直净距不足 3-交叉冲突 4-开挖破坏风险 5-占压")
    private Integer conflictType;

    @Schema(description = "冲突等级：1-一般 2-较重 3-严重 4-特别严重")
    private Integer conflictLevel;

    @Schema(description = "冲突位置经度")
    private BigDecimal lng;

    @Schema(description = "冲突位置纬度")
    private BigDecimal lat;

    @Schema(description = "冲突位置描述")
    private String location;

    @Schema(description = "水平净距(m)")
    private BigDecimal horizontalDistance;

    @Schema(description = "规范要求水平净距(m)")
    private BigDecimal requiredHorizontalDistance;

    @Schema(description = "垂直净距(m)")
    private BigDecimal verticalDistance;

    @Schema(description = "规范要求垂直净距(m)")
    private BigDecimal requiredVerticalDistance;

    @Schema(description = "冲突说明")
    private String description;

    @Schema(description = "处置建议")
    private String suggestion;

    @Schema(description = "产权单位")
    private String ownerUnit;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "处理状态：1-待处理 2-已协调 3-已处置 4-已忽略")
    private Integer status;
}
