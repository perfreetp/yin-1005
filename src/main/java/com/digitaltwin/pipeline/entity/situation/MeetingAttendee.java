package com.digitaltwin.pipeline.entity.situation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("meeting_attendee")
@Schema(description = "参会人/部门")
public class MeetingAttendee extends BaseEntity {

    @Schema(description = "会议ID")
    private Long meetingId;

    @Schema(description = "会议编号")
    private String meetingCode;

    @Schema(description = "参会者类型：1-部门 2-个人 3-群组")
    private Integer attendeeType;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "用户电话")
    private String userPhone;

    @Schema(description = "角色：1-主持 2-参会 3-列席 4-记录")
    private Integer role;

    @Schema(description = "加入状态：0-未响应 1-已确认 2-已加入 3-已离开 4-缺席")
    private Integer joinStatus;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

    @Schema(description = "离开时间")
    private LocalDateTime leaveTime;

    @Schema(description = "首次响应时间")
    private LocalDateTime responseTime;

    @Schema(description = "是否关键决策人：0-否 1-是")
    private Integer isKeyDecisionMaker;

    @Schema(description = "备注")
    private String remark;
}
