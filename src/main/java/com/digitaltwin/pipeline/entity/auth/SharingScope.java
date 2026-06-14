package com.digitaltwin.pipeline.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sharing_scope")
@Schema(description = "共享范围控制")
public class SharingScope extends BaseEntity {

    @Schema(description = "资源类型：1-管线 2-阀门 3-井盖 4-传感器 5-隐患 6-工单 7-开挖申请 8-全部")
    private Integer resourceType;

    @Schema(description = "资源ID（0表示该类型全部资源）")
    private Long resourceId;

    @Schema(description = "共享类型：1-按部门 2-按用户 3-按角色 4-公开")
    private Integer shareType;

    @Schema(description = "共享目标ID（部门ID/用户ID/角色ID）")
    private Long targetId;

    @Schema(description = "共享目标名称")
    private String targetName;

    @Schema(description = "允许的区域编码（逗号分隔，空表示全部区域）")
    private String areaCodes;

    @Schema(description = "允许的管线类型（逗号分隔，空表示全部类型）")
    private String pipelineTypes;

    @Schema(description = "权限类型：1-查看 2-编辑 3-审批 4-全部")
    private Integer permissionType;

    @Schema(description = "生效开始时间")
    private String effectiveStartTime;

    @Schema(description = "生效结束时间")
    private String effectiveEndTime;

    @Schema(description = "状态：1-启用 2-停用")
    private Integer status;

    @Schema(description = "创建人")
    private String creator;
}
