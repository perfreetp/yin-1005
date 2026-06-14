package com.digitaltwin.pipeline.service.construction.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.common.ResultCode;
import com.digitaltwin.pipeline.dto.construction.ExcavationDTO;
import com.digitaltwin.pipeline.dto.construction.ExcavationQueryDTO;
import com.digitaltwin.pipeline.dto.construction.ExcavationReviewResultDTO;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.entity.construction.EmergencyValveSuggestion;
import com.digitaltwin.pipeline.entity.construction.ExcavationApplication;
import com.digitaltwin.pipeline.entity.construction.PipelineConflict;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.mapper.construction.EmergencyValveSuggestionMapper;
import com.digitaltwin.pipeline.mapper.construction.ExcavationApplicationMapper;
import com.digitaltwin.pipeline.mapper.construction.PipelineConflictMapper;
import com.digitaltwin.pipeline.service.construction.ExcavationApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcavationApplicationServiceImpl extends ServiceImpl<ExcavationApplicationMapper, ExcavationApplication>
        implements ExcavationApplicationService {

    private final PipelineMapper pipelineMapper;
    private final ValveMapper valveMapper;
    private final PipelineConflictMapper conflictMapper;
    private final EmergencyValveSuggestionMapper valveSuggestionMapper;

    @Override
    public PageResult<ExcavationApplication> selectPage(ExcavationQueryDTO query) {
        Page<ExcavationApplication> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getExcavationType(),
                query.getAreaCode(), query.getStatus(), query.getKeyword(), query.getApplicantUnit()));
    }

    @Override
    public ExcavationApplication selectById(Long id) {
        ExcavationApplication app = super.getById(id);
        if (app == null) {
            throw new BusinessException(ResultCode.EXCAVATION_NOT_FOUND);
        }
        return app;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(ExcavationDTO dto) {
        ExcavationApplication app = new ExcavationApplication();
        BeanUtil.copyProperties(dto, app);
        app.setApplicationCode("EXC" + System.currentTimeMillis());
        app.setStatus(1);
        return this.save(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ExcavationDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        ExcavationApplication exist = super.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.EXCAVATION_NOT_FOUND);
        }
        if (exist.getStatus() > 2) {
            throw new BusinessException(ResultCode.STATUS_ERROR);
        }
        ExcavationApplication app = new ExcavationApplication();
        BeanUtil.copyProperties(dto, app);
        return this.updateById(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        ExcavationApplication exist = super.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.EXCAVATION_NOT_FOUND);
        }
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcavationReviewResultDTO review(Long applicationId) {
        ExcavationApplication app = selectById(applicationId);

        List<Pipeline> areaPipelines = pipelineMapper.selectByAreaAndType(app.getAreaCode(), null);

        List<Pipeline> involvedPipelines = areaPipelines.stream()
                .filter(p -> isPipelineInArea(p, app))
                .collect(Collectors.toList());

        List<PipelineConflict> conflicts = new ArrayList<>();
        Set<String> affectedRoads = new HashSet<>();
        Set<Integer> conflictTypes = new HashSet<>();

        for (Pipeline pipeline : involvedPipelines) {
            if (StrUtil.isNotBlank(pipeline.getRoadName())) {
                affectedRoads.add(pipeline.getRoadName());
            }

            PipelineConflict conflict = detectConflict(pipeline, app);
            if (conflict != null) {
                conflict.setConflictCode("CFT" + System.currentTimeMillis() + pipeline.getId());
                conflict.setApplicationId(app.getId());
                conflict.setApplicationCode(app.getApplicationCode());
                conflict.setPipelineId(pipeline.getId());
                conflict.setPipelineCode(pipeline.getPipelineCode());
                conflict.setPipelineName(pipeline.getPipelineName());
                conflict.setPipelineType(pipeline.getPipelineType());
                conflict.setOwnerUnit(pipeline.getOwnerUnit());
                conflict.setAreaCode(pipeline.getAreaCode());
                conflict.setStatus(1);
                conflicts.add(conflict);
                conflictTypes.add(pipeline.getPipelineType());
            }
        }

        LambdaQueryWrapper<PipelineConflict> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(PipelineConflict::getApplicationId, app.getId());
        conflictMapper.delete(deleteWrapper);
        for (PipelineConflict conflict : conflicts) {
            conflictMapper.insert(conflict);
        }

        List<EmergencyValveSuggestion> valveSuggestions = new ArrayList<>();
        Set<Long> processedPipelineIds = new HashSet<>();

        for (PipelineConflict conflict : conflicts) {
            if (conflict.getConflictLevel() >= 3 && !processedPipelineIds.contains(conflict.getPipelineId())) {
                processedPipelineIds.add(conflict.getPipelineId());

                Pipeline p = pipelineMapper.selectById(conflict.getPipelineId());
                if (p != null && Arrays.asList(1, 3, 6).contains(p.getPipelineType())) {
                    EmergencyValveSuggestion suggestion = generateValveSuggestion(p, app);
                    if (suggestion != null) {
                        suggestion.setSuggestionCode("EVS" + System.currentTimeMillis() + p.getId());
                        suggestion.setApplicationId(app.getId());
                        suggestion.setPipelineId(p.getId());
                        suggestion.setPipelineCode(p.getPipelineCode());
                        suggestion.setPipelineName(p.getPipelineName());
                        suggestion.setReasonType(1);
                        suggestion.setStatus(1);
                        valveSuggestions.add(suggestion);
                        valveSuggestionMapper.insert(suggestion);
                    }
                }
            }
        }

        app.setInvolvedPipelineCount(involvedPipelines.size());
        app.setHasConflict(conflicts.isEmpty() ? 0 : 1);
        app.setConflictPipelineTypes(conflictTypes.stream().map(String::valueOf).collect(Collectors.joining(",")));
        app.setAffectedRoadCount(affectedRoads.size());
        app.setAffectedRoadNames(String.join(",", affectedRoads));
        app.setStatus(2);
        this.updateById(app);

        ExcavationReviewResultDTO result = new ExcavationReviewResultDTO();
        result.setPassed(conflicts.stream().noneMatch(c -> c.getConflictLevel() >= 3));
        result.setInvolvedPipelineCount(involvedPipelines.size());
        result.setConflictCount(conflicts.size());
        result.setConflicts(conflicts);
        result.setAffectedRoadCount(affectedRoads.size());
        result.setAffectedRoadNames(new ArrayList<>(affectedRoads));
        result.setValveSuggestions(valveSuggestions);
        result.setReviewOpinion(generateReviewOpinion(conflicts, affectedRoads, valveSuggestions));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long applicationId, Integer passed, String opinion, String reviewer) {
        ExcavationApplication app = selectById(applicationId);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        app.setReviewOpinion(opinion);
        app.setReviewer(reviewer);
        app.setReviewTime(now);
        app.setStatus(passed == 1 ? 3 : 4);
        return this.updateById(app);
    }

    private boolean isPipelineInArea(Pipeline p, ExcavationApplication app) {
        if (p.getAreaCode() != null && app.getAreaCode() != null && p.getAreaCode().startsWith(app.getAreaCode())) {
            return true;
        }
        if (app.getCenterLng() != null && app.getCenterLat() != null
                && p.getStartLng() != null && p.getStartLat() != null) {
            double dist = calculateDistance(
                    app.getCenterLng().doubleValue(), app.getCenterLat().doubleValue(),
                    p.getStartLng().doubleValue(), p.getStartLat().doubleValue());
            return dist < 500;
        }
        return false;
    }

    private PipelineConflict detectConflict(Pipeline p, ExcavationApplication app) {
        PipelineConflict conflict = new PipelineConflict();
        conflict.setLng(p.getStartLng());
        conflict.setLat(p.getStartLat());
        conflict.setLocation(p.getRoadName());

        boolean hasConflict = false;
        int conflictLevel = 1;

        if (app.getDepth() != null && p.getBuriedDepth() != null) {
            BigDecimal depthDiff = app.getDepth().subtract(p.getBuriedDepth()).abs();
            if (depthDiff.compareTo(new BigDecimal("0.3")) < 0) {
                hasConflict = true;
                conflict.setConflictType(2);
                conflict.setVerticalDistance(depthDiff);
                conflict.setRequiredVerticalDistance(new BigDecimal("0.5"));
                conflictLevel = 2;
                if (depthDiff.compareTo(new BigDecimal("0.1")) < 0) {
                    conflictLevel = 3;
                }
            }
        }

        if (Arrays.asList(3, 4, 5).contains(p.getPipelineType())) {
            conflictLevel = Math.max(conflictLevel, 2);
            if (p.getPipelineType() == 3) {
                conflictLevel = 3;
            }
        }

        if (p.getStatus() != null && p.getStatus() == 1) {
            hasConflict = true;
            if (conflict.getConflictType() == null) {
                conflict.setConflictType(4);
            }
        }

        if (!hasConflict) {
            return null;
        }

        conflict.setConflictLevel(conflictLevel);
        conflict.setDescription(generateConflictDescription(p, app, conflict));
        conflict.setSuggestion(generateConflictSuggestion(p, conflict));

        return conflict;
    }

    private EmergencyValveSuggestion generateValveSuggestion(Pipeline p, ExcavationApplication app) {
        List<Valve> valves = valveMapper.selectByPipelineId(p.getId());
        if (CollUtil.isEmpty(valves)) {
            return null;
        }

        EmergencyValveSuggestion suggestion = new EmergencyValveSuggestion();
        List<Long> valveIds = valves.stream().limit(5).map(Valve::getId).collect(Collectors.toList());
        List<String> valveCodes = valves.stream().limit(5).map(Valve::getValveCode).collect(Collectors.toList());
        List<String> valveNames = valves.stream().limit(5).map(Valve::getValveName).collect(Collectors.toList());

        suggestion.setValveIds(valveIds.toString());
        suggestion.setValveCodes(String.join(",", valveCodes));
        suggestion.setValveNames(String.join(",", valveNames));
        suggestion.setAffectedPipelineCount(1);
        suggestion.setAffectedArea(app.getAreaName() + "周边区域");
        suggestion.setEstimatedAffectedUsers(100 + new Random().nextInt(500));
        suggestion.setEstimatedDuration(2 + new Random().nextInt(6));
        suggestion.setOperationSteps("1. 确认现场安全条件；2. 依次关闭指定阀门；3. 确认管线泄压完成；4. 设置警示标识");
        suggestion.setSafetyNotes("操作前需穿戴防护装备，作业时安排专人监护，注意检测可燃气体浓度");
        suggestion.setRecoveryPlan("施工完成后：1. 确认管线修复合格；2. 逐步开启阀门恢复供应；3. 监测压力稳定性；4. 通知受影响用户");

        return suggestion;
    }

    private String generateConflictDescription(Pipeline p, ExcavationApplication app, PipelineConflict c) {
        String[] typeNames = {"", "给水管", "排水管", "燃气管", "电力管", "通信管", "热力管", "工业管"};
        String pipelineTypeName = p.getPipelineType() != null && p.getPipelineType() < typeNames.length
                ? typeNames[p.getPipelineType()] : "管线";
        return String.format("开挖区域与%s（%s）存在冲突风险，垂直净距约%.2fm，需重点关注",
                pipelineTypeName, p.getPipelineCode(),
                c.getVerticalDistance() != null ? c.getVerticalDistance() : 0.0);
    }

    private String generateConflictSuggestion(Pipeline p, PipelineConflict c) {
        if (c.getConflictLevel() >= 3) {
            return "建议调整开挖位置或深度，与产权单位现场确认后再施工，必要时采取管线迁移或保护措施";
        } else if (c.getConflictLevel() == 2) {
            return "建议联系管线产权单位进行现场交底，施工时安排专人监护，采取人工开挖避免机械破坏";
        }
        return "施工前请联系管线产权单位确认位置，注意开挖深度控制";
    }

    private String generateReviewOpinion(List<PipelineConflict> conflicts, Set<String> roads,
                                        List<EmergencyValveSuggestion> suggestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("经校核，涉及").append(conflicts.size()).append("处管线风险点，");
        sb.append("影响").append(roads.size()).append("条道路。");

        long highRiskCount = conflicts.stream().filter(c -> c.getConflictLevel() >= 3).count();
        if (highRiskCount > 0) {
            sb.append("其中存在").append(highRiskCount).append("处高风险冲突，");
            if (!suggestions.isEmpty()) {
                sb.append("已生成").append(suggestions.size()).append("项应急关阀建议，");
            }
            sb.append("建议修改施工方案后重新提交。");
        } else if (!conflicts.isEmpty()) {
            sb.append("风险可控，建议落实现场交底和防护措施后准予施工。");
        } else {
            sb.append("未发现重大管线冲突风险，准予施工。");
        }
        return sb.toString();
    }

    private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
