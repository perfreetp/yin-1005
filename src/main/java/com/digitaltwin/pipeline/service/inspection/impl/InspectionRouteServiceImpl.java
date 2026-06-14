package com.digitaltwin.pipeline.service.inspection.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.inspection.InspectionRouteQueryDTO;
import com.digitaltwin.pipeline.entity.asset.Manhole;
import com.digitaltwin.pipeline.entity.asset.Pipeline;
import com.digitaltwin.pipeline.entity.asset.Valve;
import com.digitaltwin.pipeline.entity.inspection.InspectionRoute;
import com.digitaltwin.pipeline.mapper.asset.ManholeMapper;
import com.digitaltwin.pipeline.mapper.asset.PipelineMapper;
import com.digitaltwin.pipeline.mapper.asset.ValveMapper;
import com.digitaltwin.pipeline.mapper.inspection.InspectionRouteMapper;
import com.digitaltwin.pipeline.service.inspection.InspectionRouteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionRouteServiceImpl extends ServiceImpl<InspectionRouteMapper, InspectionRoute>
        implements InspectionRouteService {

    private final PipelineMapper pipelineMapper;
    private final ValveMapper valveMapper;
    private final ManholeMapper manholeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<InspectionRoute> selectPage(InspectionRouteQueryDTO query) {
        Page<InspectionRoute> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(baseMapper.selectPageList(page, query.getRouteType(),
                query.getPipelineType(), query.getAreaCode(), query.getStatus(), query.getKeyword()));
    }

    @Override
    public InspectionRoute selectById(Long id) {
        InspectionRoute route = super.getById(id);
        if (route == null) {
            throw new BusinessException("巡检路线不存在");
        }
        return route;
    }

    @Override
    public InspectionRoute generateRoute(String areaCode, Integer pipelineType, Integer routeType) {
        List<Pipeline> pipelines = pipelineMapper.selectByAreaAndType(areaCode, pipelineType);
        List<Valve> valves = valveMapper.selectList(null);
        List<Manhole> manholes = manholeMapper.selectList(null);

        List<Map<String, Object>> checkPoints = new ArrayList<>();
        Set<Long> addedPipelineIds = new HashSet<>();
        Set<Long> addedValveIds = new HashSet<>();
        Set<Long> addedManholeIds = new HashSet<>();

        int seq = 1;
        for (Pipeline p : pipelines) {
            if (addedPipelineIds.contains(p.getId())) continue;
            if (p.getStartLng() != null && p.getStartLat() != null) {
                Map<String, Object> point = new HashMap<>();
                point.put("id", "P" + p.getId());
                point.put("type", "pipeline");
                point.put("name", p.getPipelineName());
                point.put("lng", p.getStartLng());
                point.put("lat", p.getStartLat());
                point.put("seq", seq++);
                checkPoints.add(point);
                addedPipelineIds.add(p.getId());
            }
            if (seq > 30) break;
        }

        for (Valve v : valves) {
            if (addedValveIds.contains(v.getId())) continue;
            if (areaCode != null && v.getAreaCode() != null && !v.getAreaCode().startsWith(areaCode)) continue;
            if (v.getLng() != null && v.getLat() != null) {
                Map<String, Object> point = new HashMap<>();
                point.put("id", "V" + v.getId());
                point.put("type", "valve");
                point.put("name", v.getValveName());
                point.put("lng", v.getLng());
                point.put("lat", v.getLat());
                point.put("seq", seq++);
                checkPoints.add(point);
                addedValveIds.add(v.getId());
            }
            if (seq > 50) break;
        }

        for (Manhole m : manholes) {
            if (addedManholeIds.contains(m.getId())) continue;
            if (areaCode != null && m.getAreaCode() != null && !m.getAreaCode().startsWith(areaCode)) continue;
            if (m.getLng() != null && m.getLat() != null) {
                Map<String, Object> point = new HashMap<>();
                point.put("id", "M" + m.getId());
                point.put("type", "manhole");
                point.put("name", m.getManholeName());
                point.put("lng", m.getLng());
                point.put("lat", m.getLat());
                point.put("seq", seq++);
                checkPoints.add(point);
                addedManholeIds.add(m.getId());
            }
            if (seq > 80) break;
        }

        if (checkPoints.isEmpty()) {
            throw new BusinessException("该区域无可用巡检点");
        }

        InspectionRoute route = new InspectionRoute();
        route.setRouteCode("IR" + System.currentTimeMillis());
        route.setRouteName((routeType == 2 ? "专项" : routeType == 3 ? "重点" : "日常")
                + "巡检路线-" + (areaCode != null ? areaCode : "全部区域"));
        route.setRouteType(routeType != null ? routeType : 1);
        route.setPipelineType(pipelineType != null ? pipelineType : 7);
        route.setAreaCode(areaCode);
        route.setAreaName(areaCode != null ? areaCode + "区域" : "全部区域");

        Map<String, Object> first = checkPoints.get(0);
        Map<String, Object> last = checkPoints.get(checkPoints.size() - 1);
        route.setStartLng((BigDecimal) first.get("lng"));
        route.setStartLat((BigDecimal) first.get("lat"));
        route.setEndLng((BigDecimal) last.get("lng"));
        route.setEndLat((BigDecimal) last.get("lat"));

        double totalDist = 0;
        for (int i = 1; i < checkPoints.size(); i++) {
            Map<String, Object> prev = checkPoints.get(i - 1);
            Map<String, Object> curr = checkPoints.get(i);
            totalDist += calculateDistance(
                    ((BigDecimal) prev.get("lng")).doubleValue(),
                    ((BigDecimal) prev.get("lat")).doubleValue(),
                    ((BigDecimal) curr.get("lng")).doubleValue(),
                    ((BigDecimal) curr.get("lat")).doubleValue());
        }
        route.setTotalLength(BigDecimal.valueOf(totalDist / 1000).setScale(2, BigDecimal.ROUND_HALF_UP));
        route.setEstimatedDuration(checkPoints.size() * 5 + 30);
        route.setPipelineIds(addedPipelineIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        route.setValveIds(addedValveIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        route.setManholeIds(addedManholeIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        route.setCycleType(1);
        route.setDescription("系统自动生成的巡检路线，共" + checkPoints.size() + "个巡检点");
        route.setStatus(1);

        try {
            route.setCheckPoints(objectMapper.writeValueAsString(checkPoints));
        } catch (JsonProcessingException e) {
            route.setCheckPoints("[]");
        }

        this.save(route);
        return route;
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
