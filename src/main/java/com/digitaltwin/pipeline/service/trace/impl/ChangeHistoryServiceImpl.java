package com.digitaltwin.pipeline.service.trace.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.digitaltwin.pipeline.common.BusinessException;
import com.digitaltwin.pipeline.common.PageQuery;
import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.trace.ChangeHistoryQueryDTO;
import com.digitaltwin.pipeline.dto.trace.ChangeTimelineDTO;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;
import com.digitaltwin.pipeline.mapper.trace.ChangeHistoryMapper;
import com.digitaltwin.pipeline.service.trace.ChangeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChangeHistoryServiceImpl implements ChangeHistoryService {

    private final ChangeHistoryMapper changeHistoryMapper;

    private static final String[] RESOURCE_TYPE_NAMES = {"", "管线档案", "阀门档案", "井盖档案", "传感器", "告警记录",
            "隐患点", "开挖申请", "巡检记录", "维修工单",
            "部门", "用户", "共享权限", "巡检路线"};
    private static final String[] OPERATION_TYPE_NAMES = {"", "新增", "修改", "删除", "状态变更", "流程流转"};

    private static final Set<String> KEY_FIELDS = new HashSet<>(Arrays.asList(
            "status", "riskLevel", "riskScore", "pressureLevel",
            "alarmLevel", "urgency", "diameter", "pressure",
            "owner", "deptId", "approvalStatus"
    ));
    private static final Map<String, String> FIELD_DISPLAY_NAMES = new HashMap<>();

    static {
        FIELD_DISPLAY_NAMES.put("pipelineCode", "管线编号");
        FIELD_DISPLAY_NAMES.put("pipelineName", "管线名称");
        FIELD_DISPLAY_NAMES.put("pipelineType", "管线类型");
        FIELD_DISPLAY_NAMES.put("status", "状态");
        FIELD_DISPLAY_NAMES.put("riskLevel", "风险等级");
        FIELD_DISPLAY_NAMES.put("riskScore", "风险评分");
        FIELD_DISPLAY_NAMES.put("diameter", "口径");
        FIELD_DISPLAY_NAMES.put("pressureLevel", "压力等级");
        FIELD_DISPLAY_NAMES.put("pressure", "压力");
        FIELD_DISPLAY_NAMES.put("alarmLevel", "告警级别");
        FIELD_DISPLAY_NAMES.put("urgency", "紧急程度");
        FIELD_DISPLAY_NAMES.put("owner", "产权单位");
        FIELD_DISPLAY_NAMES.put("maintenanceUnit", "维护单位");
        FIELD_DISPLAY_NAMES.put("deptId", "部门ID");
        FIELD_DISPLAY_NAMES.put("deptName", "部门名称");
        FIELD_DISPLAY_NAMES.put("approvalStatus", "审批状态");
        FIELD_DISPLAY_NAMES.put("operationStatus", "运行状态");
        FIELD_DISPLAY_NAMES.put("valveStatus", "阀门状态");
        FIELD_DISPLAY_NAMES.put("title", "标题");
        FIELD_DISPLAY_NAMES.put("description", "描述");
        FIELD_DISPLAY_NAMES.put("lng", "经度");
        FIELD_DISPLAY_NAMES.put("lat", "纬度");
        FIELD_DISPLAY_NAMES.put("depth", "埋深");
        FIELD_DISPLAY_NAMES.put("age", "服役年限");
        FIELD_DISPLAY_NAMES.put("material", "材质");
    }

    @Override
    public PageResult<ChangeHistory> selectPage(ChangeHistoryQueryDTO query) {
        if (query == null) query = new ChangeHistoryQueryDTO();
        query.setPageNum(query.getPageNum() != null ? query.getPageNum() : 1);
        query.setPageSize(query.getPageSize() != null ? query.getPageSize() : 10);

        LambdaQueryWrapper<ChangeHistory> wrapper = new LambdaQueryWrapper<>();
        if (query.getResourceType() != null) wrapper.eq(ChangeHistory::getResourceType, query.getResourceType());
        if (query.getResourceId() != null) wrapper.eq(ChangeHistory::getResourceId, query.getResourceId());
        if (StrUtil.isNotBlank(query.getOperation())) {
            wrapper.like(ChangeHistory::getOperation, query.getOperation());
        }
        if (StrUtil.isNotBlank(query.getOperatorName())) {
            wrapper.like(ChangeHistory::getOperatorName, query.getOperatorName());
        }
        if (StrUtil.isNotBlank(query.getStartTime())) {
            wrapper.ge(ChangeHistory::getCreateTime, query.getStartTime());
        }
        if (StrUtil.isNotBlank(query.getEndTime())) {
            wrapper.le(ChangeHistory::getCreateTime, query.getEndTime());
        }
        wrapper.orderByDesc(ChangeHistory::getCreateTime);

        Page<ChangeHistory> page = new Page<>(query.getPageNum(), query.getPageSize());
        return PageResult.of(changeHistoryMapper.selectPage(page, wrapper));
    }

    @Override
    public ChangeTimelineDTO getResourceTimeline(Integer resourceType, Long resourceId) {
        if (resourceType == null || resourceId == null) {
            throw new BusinessException("资源类型和ID不能为空");
        }

        ChangeTimelineDTO result = new ChangeTimelineDTO();
        result.setResourceType(resourceType);
        result.setResourceTypeName(resourceType < RESOURCE_TYPE_NAMES.length ? RESOURCE_TYPE_NAMES[resourceType] : "未知");
        result.setResourceId(resourceId);

        List<ChangeHistory> all = changeHistoryMapper.selectList(
                new LambdaQueryWrapper<ChangeHistory>()
                        .eq(ChangeHistory::getResourceType, resourceType)
                        .eq(ChangeHistory::getResourceId, resourceId)
                        .orderByDesc(ChangeHistory::getCreateTime));

        result.setTotalChangeCount(all.size());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        int c7 = 0, c30 = 0;
        Map<String, Integer> operatorCount = new LinkedHashMap<>();
        List<ChangeTimelineDTO.TimelineItem> items = new ArrayList<>();

        for (ChangeHistory ch : all) {
            try {
                if (ch.getCreateTime() != null) {
                    LocalDateTime ct = LocalDateTime.parse(ch.getCreateTime(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (ct.isAfter(sevenDaysAgo)) c7++;
                    if (ct.isAfter(thirtyDaysAgo)) c30++;
                }
            } catch (Exception ignored) { }

            if (StrUtil.isNotBlank(ch.getOperatorName())) {
                operatorCount.merge(ch.getOperatorName(), 1, Integer::sum);
            }

            if (StrUtil.isBlank(result.getResourceName()) && StrUtil.isNotBlank(ch.getResourceName())) {
                result.setResourceName(ch.getResourceName());
            }

            ChangeTimelineDTO.TimelineItem item = new ChangeTimelineDTO.TimelineItem();
            item.setChangeId(ch.getId());
            try {
                if (StrUtil.isNotBlank(ch.getCreateTime())) {
                    item.setChangeTime(LocalDateTime.parse(ch.getCreateTime(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            } catch (Exception ignored) { }
            item.setOperationType(ch.getOperationType());
            item.setOperationTypeName(ch.getOperationType() != null && ch.getOperationType() < OPERATION_TYPE_NAMES.length
                    ? OPERATION_TYPE_NAMES[ch.getOperationType()] : "未知");
            item.setOperation(ch.getOperation());
            item.setOperatorId(ch.getOperatorId());
            item.setOperatorName(ch.getOperatorName());
            item.setBeforeSummary(summarizeJson(ch.getBeforeValue()));
            item.setAfterSummary(summarizeJson(ch.getAfterValue()));
            item.setFieldDiffs(computeFieldDiffs(ch.getBeforeValue(), ch.getAfterValue()));
            item.setIpAddress(ch.getIpAddress());
            item.setRemark(ch.getRemark());
            items.add(item);
        }

        result.setLast7DaysCount(c7);
        result.setLast30DaysCount(c30);
        result.setItems(items);

        List<ChangeTimelineDTO.OperatorStat> topOps = new ArrayList<>();
        operatorCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> {
                    ChangeTimelineDTO.OperatorStat s = new ChangeTimelineDTO.OperatorStat();
                    s.setOperatorName(e.getKey());
                    s.setCount(e.getValue());
                    topOps.add(s);
                });
        result.setTopOperators(topOps);

        return result;
    }

    @Override
    public void recordChange(Integer resourceType, Long resourceId, String resourceName,
                           Integer operationType, String operation,
                           String beforeValue, String afterValue,
                           Long operatorId, String operatorName, String ipAddress,
                           String remark) {
        ChangeHistory ch = new ChangeHistory();
        ch.setResourceType(resourceType);
        ch.setResourceId(resourceId);
        ch.setResourceName(resourceName);
        ch.setOperationType(operationType);
        ch.setOperation(operation);
        ch.setBeforeValue(beforeValue);
        ch.setAfterValue(afterValue);
        ch.setOperatorId(operatorId);
        ch.setOperatorName(operatorName);
        ch.setIpAddress(ipAddress);
        ch.setRemark(remark);
        changeHistoryMapper.insert(ch);
    }

    private String summarizeJson(String json) {
        if (StrUtil.isBlank(json)) return "-";
        try {
            Object obj = JSONUtil.parse(json);
            if (obj instanceof JSONObject jo) {
                List<String> parts = new ArrayList<>();
                int count = 0;
                for (String key : jo.keySet()) {
                    Object v = jo.get(key);
                    if (v == null) continue;
                    String vs = v.toString();
                    if (vs.length() > 30) vs = vs.substring(0, 30) + "...";
                    parts.add(FIELD_DISPLAY_NAMES.getOrDefault(key, key) + "=" + vs);
                    count++;
                    if (count >= 5) break;
                }
                if (parts.isEmpty()) return "(无关键字段)";
                return String.join("; ", parts);
            }
            return json.length() > 100 ? json.substring(0, 100) + "..." : json;
        } catch (Exception e) {
            return json.length() > 100 ? json.substring(0, 100) + "..." : json;
        }
    }

    private List<ChangeTimelineDTO.FieldDiff> computeFieldDiffs(String beforeJson, String afterJson) {
        List<ChangeTimelineDTO.FieldDiff> result = new ArrayList<>();
        if (StrUtil.isBlank(beforeJson) || StrUtil.isBlank(afterJson)) return result;

        try {
            JSONObject beforeObj = JSONUtil.parseObj(beforeJson, true);
            JSONObject afterObj = JSONUtil.parseObj(afterJson, true);

            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(beforeObj.keySet());
            allKeys.addAll(afterObj.keySet());

            for (String key : allKeys) {
                if ("createTime".equals(key) || "updateTime".equals(key)
                        || "createBy".equals(key) || "updateBy".equals(key)
                        || "deleted".equals(key)) continue;

                Object bv = beforeObj.get(key);
                Object av = afterObj.get(key);
                String bvs = bv != null ? bv.toString() : "";
                String avs = av != null ? av.toString() : "";
                if (bvs.equals(avs)) continue;
                if (StrUtil.isBlank(bvs) && StrUtil.isBlank(avs)) continue;

                ChangeTimelineDTO.FieldDiff fd = new ChangeTimelineDTO.FieldDiff();
                fd.setFieldName(key);
                fd.setDisplayName(FIELD_DISPLAY_NAMES.getOrDefault(key, key));
                fd.setBeforeValue(bvs.length() > 50 ? bvs.substring(0, 50) + "..." : bvs);
                fd.setAfterValue(avs.length() > 50 ? avs.substring(0, 50) + "..." : avs);
                fd.setIsKey(KEY_FIELDS.contains(key) ? 1 : 0);
                result.add(fd);
            }
        } catch (Exception ignored) { }
        result.sort((a, b) -> Integer.compare(b.getIsKey(), a.getIsKey()));
        return result;
    }
}
