package com.digitaltwin.pipeline.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.digitaltwin.pipeline.common.ChangeLog;
import com.digitaltwin.pipeline.entity.trace.ChangeHistory;
import com.digitaltwin.pipeline.service.trace.ChangeHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ChangeLogAspect {

    private final ChangeHistoryService changeHistoryService;

    @Around("@annotation(changeLog)")
    public Object around(ProceedingJoinPoint pjp, ChangeLog changeLog) throws Throwable {
        Object[] args = pjp.getArgs();
        Object result;
        Object beforeObj = null;
        Long resourceId = null;
        String resourceName = null;
        String beforeJson = null;

        try {
            beforeObj = extractObjectByIdFromArgs(args);
            beforeJson = toJson(beforeObj);
            resourceId = extractId(beforeObj);
            resourceName = extractName(beforeObj);
        } catch (Exception e) {
            log.warn("ChangeLog before extract failed: {}", e.getMessage());
        }

        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        }

        try {
            Object afterObj = null;
            if (result != null) afterObj = result;
            else afterObj = extractObjectByIdFromArgs(args);

            if (resourceId == null) resourceId = extractId(afterObj);
            if (StrUtil.isBlank(resourceName)) resourceName = extractName(afterObj);

            if (resourceId == null) {
                for (Object arg : args) {
                    if (arg instanceof Long lid) { resourceId = lid; break; }
                }
            }

            String afterJson = toJson(afterObj);
            if (StrUtil.isBlank(beforeJson)) beforeJson = afterJson;

            int opType = resolveOperationType(changeLog.operation());

            changeHistoryService.recordChange(
                    safeParseInt(changeLog.resourceType()),
                    resourceId,
                    resourceName,
                    opType,
                    changeLog.operation(),
                    changeLog.captureDiff() ? beforeJson : null,
                    changeLog.captureDiff() ? afterJson : null,
                    1L,
                    "系统用户",
                    getClientIp(),
                    null
            );
        } catch (Exception e) {
            log.error("ChangeLog record error: {}", e.getMessage(), e);
        }

        return result;
    }

    private Object extractObjectByIdFromArgs(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg == null) continue;
            if (arg instanceof Long || arg instanceof String || arg instanceof Integer) continue;
            if (arg.getClass().getName().startsWith("com.digitaltwin.pipeline.entity") ||
                arg.getClass().getName().startsWith("com.digitaltwin.pipeline.dto")) {
                return arg;
            }
        }
        return null;
    }

    private Long extractId(Object obj) {
        if (obj == null) return null;
        try {
            Method m = obj.getClass().getMethod("getId");
            Object v = m.invoke(obj);
            if (v instanceof Long) return (Long) v;
            if (v instanceof Integer) return ((Integer) v).longValue();
        } catch (Exception ignored) { }
        return null;
    }

    private String extractName(Object obj) {
        if (obj == null) return null;
        List<String> candidateMethods = Arrays.asList("getPipelineCode", "getValveCode", "getManholeCode",
                "getSensorCode", "getAlarmCode", "getHazardCode",
                "getApplicationCode", "getOrderCode", "getRouteCode",
                "getName", "getTitle", "getPipelineName", "getValveName");
        for (String mn : candidateMethods) {
            try {
                Method m = obj.getClass().getMethod(mn);
                Object v = m.invoke(obj);
                if (v instanceof String s && StrUtil.isNotBlank(s)) return s;
            } catch (Exception ignored) { }
        }
        return null;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            String json = JSONUtil.toJsonStr(obj);
            if (json.length() > 8000) json = json.substring(0, 8000);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    private int resolveOperationType(String op) {
        if (StrUtil.isBlank(op)) return 2;
        String lower = op.toLowerCase();
        if (lower.contains("新增") || lower.contains("create") || lower.contains("save") || lower.contains("add")) return 1;
        if (lower.contains("删除") || lower.contains("delete") || lower.contains("remove")) return 3;
        if (lower.contains("状态") || lower.contains("审批") || lower.contains("审核")) return 4;
        if (lower.contains("流转") || lower.contains("派单") || lower.contains("处置") || lower.contains("工单")) return 5;
        return 2;
    }

    private Integer safeParseInt(String s) {
        try {
            if (StrUtil.isBlank(s)) return 0;
            BigDecimal bd = new BigDecimal(s);
            return bd.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                int i = ip.indexOf(',');
                return i > 0 ? ip.substring(0, i) : ip;
            }
            return req.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
