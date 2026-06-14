package com.digitaltwin.pipeline.service.asset.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.asset.ManholeQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import com.digitaltwin.pipeline.mapper.asset.ManholeMapper;
import com.digitaltwin.pipeline.service.asset.ManholeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManholeServiceImpl extends ServiceImpl<ManholeMapper, Manhole> implements ManholeService {

    @Override
    public PageResult<Manhole> selectPage(ManholeQueryDTO query) {
        Page<Manhole> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getManholeType(),
                query.getAreaCode(), query.getStatus(), query.getKeyword()));
    }

    @Override
    public Manhole selectById(Long id) {
        Manhole manhole = super.getById(id);
        if (manhole == null) {
            throw new BusinessException(ResultCode.MANHOLE_NOT_FOUND);
        }
        return manhole;
    }

    @Override
    public List<Manhole> selectByAreaAndType(String areaCode, Integer manholeType) {
        return baseMapper.selectByAreaAndType(areaCode, manholeType);
    }
}
