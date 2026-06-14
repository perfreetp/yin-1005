package com.digitaltwin.pipeline.service.asset.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.asset.ValveQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.service.asset.ValveService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValveServiceImpl extends ServiceImpl<ValveMapper, Valve> implements ValveService {

    @Override
    public PageResult<Valve> selectPage(ValveQueryDTO query) {
        Page<Valve> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getValveType(),
                query.getAreaCode(), query.getStatus(), query.getKeyword()));
    }

    @Override
    public Valve selectById(Long id) {
        Valve valve = super.getById(id);
        if (valve == null) {
            throw new BusinessException(ResultCode.VALVE_NOT_FOUND);
        }
        return valve;
    }

    @Override
    public List<Valve> selectByPipelineId(Long pipelineId) {
        return baseMapper.selectByPipelineId(pipelineId);
    }

    @Override
    public List<Valve> selectEmergencyValves(Double lng, Double lat, Integer pipelineType, Double radius) {
        return baseMapper.selectEmergencyValves(lng, lat, pipelineType, radius);
    }
}
