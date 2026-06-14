package com.digitaltwin.pipeline.entity.inspection;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order")
@Schema(description = "维修工单")
public class WorkOrder extends BaseEntity {

    @Schema(description = "工单编号")
    private String orderCode;

    @Schema(description = "工单标题")
    private String title;

    @Schema(description = "工单类型：1-维修 2-抢修 3-巡检整改 4-改造 5-日常维护")
    private Integer orderType;

    @Schema(description = "工单来源：1-告警 2-隐患 3-缺陷 4-巡检 5-开挖施工 6-其他")
    private Integer orderSource;

    @Schema(description = "紧急程度：1-一般 2-较重 3-严重 4-紧急")
    private Integer urgency;

    @Schema(description = "工单描述")
    private String description;

    @Schema(description = "经度")
    private java.math.BigDecimal lng;

    @Schema(description = "纬度")
    private java.math.BigDecimal lat;

    @Schema(description = "位置描述")
    private String location;

    @Schema(description = "关联管线ID")
    private Long pipelineId;

    @Schema(description = "关联管线编号")
    private String pipelineCode;

    @Schema(description = "关联井盖ID")
    private Long manholeId;

    @Schema(description = "关联阀门ID")
    private Long valveId;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "关联告警ID")
    private Long alarmId;

    @Schema(description = "关联隐患ID")
    private Long hazardId;

    @Schema(description = "关联缺陷ID")
    private Long defectId;

    @Schema(description = "关联开挖申请ID")
    private Long applicationId;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private String createOrderTime;

    @Schema(description = "派单时间")
    private String dispatchTime;

    @Schema(description = "派单人")
    private String dispatcher;

    @Schema(description = "承办部门")
    private String undertakeDept;

    @Schema(description = "承办人")
    private String undertaker;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "预计完成时间")
    private String expectCompleteTime;

    @Schema(description = "实际开始时间")
    private String actualStartTime;

    @Schema(description = "实际完成时间")
    private String actualCompleteTime;

    @Schema(description = "用时(小时)")
    private java.math.BigDecimal usedHours;

    @Schema(description = "材料消耗（JSON数组）")
    private String materials;

    @Schema(description = "人工消耗")
    private String laborCost;

    @Schema(description = "施工人员")
    private String constructionWorkers;

    @Schema(description = "处置方案说明")
    private String disposalPlan;

    @Schema(description = "处置过程描述")
    private String disposalProcess;

    @Schema(description = "处置结果说明")
    private String disposalResult;

    @Schema(description = "验收人")
    private String acceptor;

    @Schema(description = "验收时间")
    private String acceptTime;

    @Schema(description = "验收意见")
    private String acceptOpinion;

    @Schema(description = "验收是否合格：0-否 1-是")
    private Integer acceptQualified;

    @Schema(description = "施工前图片（JSON数组）")
    private String beforeImages;

    @Schema(description = "施工中图片（JSON数组）")
    private String duringImages;

    @Schema(description = "施工后图片（JSON数组）")
    private String afterImages;

    @Schema(description = "工单状态：1-待派单 2-已派单 3-处理中 4-待验收 5-已完成 6-已取消 7-已驳回")
    private Integer status;

    @Schema(description = "当前处理节点")
    private String currentNode;

    @Schema(description = "流程进度(%)")
    private Integer progress;

    @Schema(description = "备注")
    private String remark;
}
