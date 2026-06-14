package com.digitaltwin.pipeline.service.asset.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.asset.PipelineDTO;
import com.digitaltwin.pipeline.dto.asset.PipelineQueryDTO;
import com.digitaltwin.pipeline.dto.asset.TopologyResultDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.asset.PipelineNode;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.mapper.asset.ManholeMapper;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.asset.PipelineNodeMapper;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.service.asset.PipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PipelineServiceImpl extends ServiceImpl<PipelineMapper, Pipeline> implements PipelineService {

    private final PipelineNodeMapper pipelineNodeMapper;
    private final ValveMapper valveMapper;
    private final ManholeMapper manholeMapper;

    @Override
    public PageResult<Pipeline> selectPage(PipelineQueryDTO query) {
        Page<Pipeline> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getPipelineType(),
                query.getAreaCode(), query.getStatus(), query.getKeyword()));
    }

    @Override
    public Pipeline selectById(Long id) {
        Pipeline pipeline = super.getById(id);
        if (pipeline == null) {
            throw new BusinessException(ResultCode.PIPELINE_NOT_FOUND);
        }
        return pipeline;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(PipelineDTO dto) {
        Pipeline pipeline = new Pipeline();
        BeanUtil.copyProperties(dto, pipeline);
        return this.save(pipeline);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(PipelineDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Pipeline exist = super.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.PIPELINE_NOT_FOUND);
        }
        Pipeline pipeline = new Pipeline();
        BeanUtil.copyProperties(dto, pipeline);
        return this.updateById(pipeline);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        Pipeline exist = super.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.PIPELINE_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    public List<Pipeline> selectByAreaAndType(String areaCode, Integer pipelineType) {
        return baseMapper.selectByAreaAndType(areaCode, pipelineType);
    }

    @Override
    public TopologyResultDTO queryTopology(String areaCode, Integer pipelineType, String nodeCode) {
        TopologyResultDTO result = new TopologyResultDTO();

        List<Pipeline> pipelines;
        if (StrUtil.isNotBlank(nodeCode)) {
            pipelines = baseMapper.selectConnectedPipelines(nodeCode);
        } else {
            pipelines = baseMapper.selectByAreaAndType(areaCode, pipelineType);
        }
        result.setPipelines(pipelines);

        List<PipelineNode> nodes = pipelineNodeMapper.selectByAreaCode(areaCode);
        result.setNodes(nodes);

        List<Valve> valves = valveMapper.selectList(null);
        result.setValves(valves);

        List<Manhole> manholes = manholeMapper.selectList(null);
        result.setManholes(manholes);

        return result;
    }

    @Override
    public List<Pipeline> filterList(String areaCode, Integer pipelineType, String keyword, Integer status, Integer limit) {
        LambdaQueryWrapper<Pipeline> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(areaCode)) {
            wrapper.eq(Pipeline::getAreaCode, areaCode);
        }
        if (pipelineType != null) {
            wrapper.eq(Pipeline::getPipelineType, pipelineType);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Pipeline::getPipelineCode, keyword)
                    .or().like(Pipeline::getPipelineName, keyword)
                    .or().like(Pipeline::getRoadName, keyword));
        }
        if (status != null) {
            wrapper.eq(Pipeline::getStatus, status);
        }
        wrapper.orderByDesc(Pipeline::getId);
        wrapper.last("LIMIT " + limit);
        return baseMapper.selectList(wrapper);
    }
}
