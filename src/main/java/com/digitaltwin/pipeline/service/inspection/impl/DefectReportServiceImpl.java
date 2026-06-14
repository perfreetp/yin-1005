package com.digitaltwin.pipeline.service.inspection.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.inspection.DefectQueryDTO;
import com.digitaltwin.pipeline.dto.inspection.DefectReportDTO;
import com.digitaltwin.pipeline.entity.inspection.DefectReport;
import com.digitaltwin.pipeline.mapper.inspection.DefectReportMapper;
import com.digitaltwin.pipeline.service.inspection.DefectReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DefectReportServiceImpl extends ServiceImpl<DefectReportMapper, DefectReport>
        implements DefectReportService {

    @Override
    public PageResult<DefectReport> selectPage(DefectQueryDTO query) {
        Page<DefectReport> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getDefectType(),
                query.getDefectLevel(), query.getAreaCode(), query.getStatus(),
                query.getReportSource(), query.getKeyword()));
    }

    @Override
    public DefectReport selectById(Long id) {
        DefectReport defect = super.getById(id);
        if (defect == null) {
            throw new BusinessException("缺陷记录不存在");
        }
        return defect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefectReport report(DefectReportDTO dto) {
        DefectReport defect = new DefectReport();
        BeanUtil.copyProperties(dto, defect);
        defect.setDefectCode("DF" + System.currentTimeMillis());
        defect.setReportTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        defect.setStatus(1);
        if (defect.getDefectLevel() == null) {
            defect.setDefectLevel(calculateDefectLevel(dto.getDefectType()));
        }
        this.save(defect);
        return defect;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receive(Long id, String receiver) {
        DefectReport defect = super.getById(id);
        if (defect == null) {
            throw new BusinessException("缺陷记录不存在");
        }
        defect.setStatus(2);
        defect.setReceiver(receiver);
        defect.setReceiveTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return this.updateById(defect);
    }

    private Integer calculateDefectLevel(Integer defectType) {
        if (defectType == null) return 1;
        return switch (defectType) {
            case 2, 6 -> 4;
            case 1, 3, 7 -> 3;
            case 4, 5, 8 -> 2;
            default -> 1;
        };
    }
}
