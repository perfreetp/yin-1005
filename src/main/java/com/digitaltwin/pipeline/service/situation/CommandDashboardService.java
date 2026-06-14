package com.digitaltwin.pipeline.service.situation;

import com.digitaltwin.pipeline.dto.situation.CommandDashboardVO;

import java.util.List;

public interface CommandDashboardService {

    CommandDashboardVO getDashboard(String areaCode, Integer minLevel, List<Integer> eventTypes);
}
