package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_record")
@Schema(description = "巡检记录")
public class InspectionRecord extends BaseEntity {

    @Schema(description = "记录编号")
    private String recordCode;

    @Schema(description = "关联路线ID")
    private Long routeId;

    @Schema(description = "关联路线编号")
    private String routeCode;

    @Schema(description = "关联路线名称")
    private String routeName;

    @Schema(description = "巡检人ID")
    private Long inspectorId;

    @Schema(description = "巡检人姓名")
    private String inspectorName;

    @Schema(description = "巡检开始时间")
    private String startTime;

    @Schema(description = "巡检结束时间")
    private String endTime;

    @Schema(description = "巡检时长(分钟)")
    private Integer duration;

    @Schema(description = "实际巡检里程(km)")
    private java.math.BigDecimal actualDistance;

    @Schema(description = "巡检轨迹坐标（JSON数组）")
    private String trajectory;

    @Schema(description = "应巡检点数")
    private Integer totalPoints;

    @Schema(description = "已巡检点数")
    private Integer checkedPoints;

    @Schema(description = "巡检完成率(%)")
    private Integer completionRate;

    @Schema(description = "发现缺陷数量")
    private Integer defectCount;

    @Schema(description = "已上报缺陷数量")
    private Integer reportedDefectCount;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "巡检情况说明")
    private String description;

    @Schema(description = "巡检状态：1-进行中 2-已完成 3-异常中断 4-已取消")
    private Integer status;
}
