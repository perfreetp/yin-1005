package com.digitaltwin.pipeline.entity.linkage;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("linkage_notification")
@Schema(description = "联动通知记录：发给部门/个人的消息")
public class LinkageNotification extends BaseEntity {

    @Schema(description = "关联联动任务ID")
    private Long taskId;

    @Schema(description = "关联联动任务编号")
    private String taskCode;

    @Schema(description = "通知类型：1-短信 2-系统消息 3-邮件 4-钉钉 5-微信 6-电话记录")
    private Integer notifyType;

    @Schema(description = "接收方类型：1-部门 2-个人 3-群组")
    private Integer receiverType;

    @Schema(description = "接收方ID")
    private Long receiverId;

    @Schema(description = "接收方名称")
    private String receiverName;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "消息级别：1-普通 2-重要 3-紧急")
    private Integer level;

    @Schema(description = "发送状态：0-待发送 1-已发送 2-发送失败 3-已读 4-已确认")
    private Integer sendStatus;

    @Schema(description = "发送时间")
    private String sendTime;

    @Schema(description = "阅读时间")
    private String readTime;

    @Schema(description = "确认时间")
    private String confirmTime;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "发送次数")
    private Integer retryCount;
}
