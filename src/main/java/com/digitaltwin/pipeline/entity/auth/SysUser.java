package com.digitaltwin.pipeline.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser extends BaseEntity {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "角色ID列表（逗号分隔）")
    private String roleIds;

    @Schema(description = "负责区域编码（逗号分隔）")
    private String areaCodes;

    @Schema(description = "负责管线类型（逗号分隔）")
    private String pipelineTypes;

    @Schema(description = "状态：1-启用 2-停用")
    private Integer status;

    @Schema(description = "最后登录时间")
    private String lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "备注")
    private String remark;
}
