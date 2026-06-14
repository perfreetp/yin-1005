package com.digitaltwin.pipeline.service.asset;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.asset.PipelineDTO;
import com.digitaltwin.pipeline.dto.asset.PipelineQueryDTO;
import com.digitaltwin.pipeline.dto.asset.TopologyResultDTO;
import com.digitaltwin.pipeline.entity.asset.Pipeline;

import java.util.List;

public interface PipelineService extends IService<Pipeline> {

    PageResult<Pipeline> selectPage(PipelineQueryDTO query);

    Pipeline selectById(Long id);

    boolean create(PipelineDTO dto);

    boolean update(PipelineDTO dto);

    boolean deleteById(Long id);

    List<Pipeline> selectByAreaAndType(String areaCode, Integer pipelineType);

    TopologyResultDTO queryTopology(String areaCode, Integer pipelineType, String nodeCode);
}
