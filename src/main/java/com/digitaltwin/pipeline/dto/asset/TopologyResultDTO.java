package com.digitaltwin.pipeline.dto.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "管网拓扑查询结果")
public class TopologyResultDTO {

    @Schema(description = "管线列表")
    private List<com.digitaltwin.pipeline.entity.asset.Pipeline> pipelines;

    @Schema(description = "节点列表")
    private List<com.digitaltwin.pipeline.entity.asset.PipelineNode> nodes;

    @Schema(description = "阀门列表")
    private List<com.digitaltwin.pipeline.entity.asset.Valve> valves;

    @Schema(description = "井盖列表")
    private List<com.digitaltwin.pipeline.entity.asset.Manhole> manholes;
}
