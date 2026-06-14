package com.digitaltwin.pipeline.entity.asset;

import com.baomidou.mybatisplus.annotation.TableName;
import com.digitaltwin.pipeline.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pipeline_node")
@Schema(description = "管网节点实体")
public class PipelineNode extends BaseEntity {

    @Schema(description = "节点编号")
    private String nodeCode;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型：1-三通 2-四通 3-弯头 4-异径 5-端点 6-其他")
    private Integer nodeType;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "高程(m)")
    private BigDecimal elevation;

    @Schema(description = "埋设深度(m)")
    private BigDecimal buriedDepth;

    @Schema(description = "所属区域编码")
    private String areaCode;

    @Schema(description = "所属区域名称")
    private String areaName;

    @Schema(description = "连接的管线ID列表（逗号分隔）")
    private String connectedPipelineIds;

    @Schema(description = "状态：1-正常 2-维修中 3-停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
