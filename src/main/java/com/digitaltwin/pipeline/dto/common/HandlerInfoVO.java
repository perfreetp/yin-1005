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
@Schema(description = "统一处理人信息（所有有承办/操作人的对象共用）")
public class HandlerInfoVO implements Serializable {

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "人员ID")
    private Long userId;

    @Schema(description = "人员姓名")
    private String userName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "角色/职务")
    private String role;
}
