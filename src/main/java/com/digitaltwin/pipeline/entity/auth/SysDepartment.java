package com.digitaltwin.pipeline.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
@Schema(description = "部门")
public class SysDepartment extends BaseEntity {

    @Schema(description = "部门编号")
    private String deptCode;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "父级部门ID")
    private Long parentId;

    @Schema(description = "部门类型：1-管理部门 2-运维部门 3-审批部门 4-施工单位 5-产权单位")
    private Integer deptType;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "负责区域编码（逗号分隔）")
    private String areaCodes;

    @Schema(description = "负责管线类型（逗号分隔）")
    private String pipelineTypes;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1-启用 2-停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
