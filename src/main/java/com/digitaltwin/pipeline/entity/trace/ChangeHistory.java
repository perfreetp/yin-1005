package com.digitaltwin.pipeline.entity.trace;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("change_history")
@Schema(description = "变更历史记录")
public class ChangeHistory extends BaseEntity {

    @Schema(description = "关联业务类型：1-管线 2-阀门 3-井盖 4-传感器 5-隐患 6-工单 7-开挖申请 8-巡检记录")
    private Integer businessType;

    @Schema(description = "关联业务ID")
    private Long businessId;

    @Schema(description = "关联业务编号")
    private String businessCode;

    @Schema(description = "变更类型：1-新增 2-修改 3-删除 4-状态变更 5-流程流转")
    private Integer changeType;

    @Schema(description = "变更字段名")
    private String fieldName;

    @Schema(description = "变更前值")
    private String oldValue;

    @Schema(description = "变更后值")
    private String newValue;

    @Schema(description = "变更说明")
    private String description;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "操作人所属部门")
    private String operatorDept;

    @Schema(description = "操作时间")
    private String operateTime;

    @Schema(description = "操作IP")
    private String operateIp;

    @Schema(description = "所属区域编码")
    private String areaCode;
}
