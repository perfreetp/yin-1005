package com.digitaltwin.pipeline.service.sensor.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.sensor.HazardDTO;
import com.digitaltwin.pipeline.dto.sensor.HazardQueryDTO;
import com.digitaltwin.pipeline.entity.sensor.Hazard;
import com.digitaltwin.pipeline.mapper.sensor.HazardMapper;
import com.digitaltwin.pipeline.service.sensor.HazardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HazardServiceImpl extends ServiceImpl<HazardMapper, Hazard> implements HazardService {

    @Override
    public PageResult<Hazard> selectPage(HazardQueryDTO query) {
        Page<Hazard> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getHazardType(),
                query.getRiskLevel(), query.getAreaCode(), query.getStatus(), query.getKeyword()));
    }

    @Override
    public Hazard selectById(Long id) {
        Hazard hazard = super.getById(id);
        if (hazard == null) {
            throw new BusinessException(ResultCode.HAZARD_NOT_FOUND);
        }
        return hazard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(HazardDTO dto) {
        Hazard hazard = new Hazard();
        BeanUtil.copyProperties(dto, hazard);
        hazard.setHazardCode("HZ" + System.currentTimeMillis());
        if (dto.getDiscoverTime() == null) {
            hazard.setDiscoverTime(java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (hazard.getRiskScore() == null || hazard.getRiskScore() == 0) {
            hazard.setRiskScore(calculateRiskScore(hazard));
        }
        if (hazard.getRiskLevel() == null) {
            hazard.setRiskLevel(mapRiskLevel(hazard.getRiskScore()));
        }
        if (hazard.getStatus() == null) {
            hazard.setStatus(1);
        }
        return this.save(hazard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(HazardDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Hazard exist = super.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.HAZARD_NOT_FOUND);
        }
        Hazard hazard = new Hazard();
        BeanUtil.copyProperties(dto, hazard);
        if (hazard.getRiskScore() == null || hazard.getRiskScore() == 0) {
            hazard.setRiskScore(calculateRiskScore(hazard));
        }
        if (hazard.getRiskLevel() == null) {
            hazard.setRiskLevel(mapRiskLevel(hazard.getRiskScore()));
        }
        return this.updateById(hazard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        Hazard exist = super.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.HAZARD_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    public int calculateRiskScore(Hazard hazard) {
        int score = 0;

        if (hazard.getHazardType() != null) {
            score += switch (hazard.getHazardType()) {
                case 2 -> 30;
                case 7 -> 25;
                case 1 -> 20;
                case 3, 4 -> 15;
                case 5, 6 -> 20;
                default -> 10;
            };
        }

        if (hazard.getUrgency() != null) {
            score += hazard.getUrgency() * 10;
        } else {
            score += 10;
        }

        if (hazard.getAffectScope() != null && !hazard.getAffectScope().isEmpty()) {
            if (hazard.getAffectScope().contains("大面积") || hazard.getAffectScope().contains("重大")) {
                score += 25;
            } else if (hazard.getAffectScope().contains("中等") || hazard.getAffectScope().contains("一定")) {
                score += 15;
            } else {
                score += 5;
            }
        } else {
            score += 5;
        }

        return Math.min(score, 100);
    }

    private Integer mapRiskLevel(Integer score) {
        if (score >= 75) return 4;
        if (score >= 50) return 3;
        if (score >= 25) return 2;
        return 1;
    }
}
