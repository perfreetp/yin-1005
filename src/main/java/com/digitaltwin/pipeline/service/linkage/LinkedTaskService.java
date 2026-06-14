package com.digitaltwin.pipeline.service.linkage;

import com.digitaltwin.pipeline.common.PageResult;
import com.digitaltwin.pipeline.dto.linkage.LinkedTaskDetailVO;
import com.digitaltwin.pipeline.dto.linkage.LinkedTaskTriggerDTO;
import com.digitaltwin.pipeline.entity.linkage.LinkedTask;
import com.digitaltwin.pipeline.entity.linkage.LinkageNotification;
import com.digitaltwin.pipeline.common.PageQuery;

import java.util.List;

public interface LinkedTaskService {

    LinkedTask triggerFromSource(LinkedTaskTriggerDTO dto);

    LinkedTaskDetailVO getDetail(Long taskId);

    PageResult<LinkedTask> selectPage(PageQuery query, Integer status, Integer priority,
                                      Integer taskType, Integer sourceType, String areaCode);

    List<LinkageNotification> getTaskNotifications(Long taskId);

    LinkedTask assignTask(Long taskId, Long deptId, Long userId, String operatorName);

    LinkedTask reportProgress(Long taskId, Integer progress, String content, String operatorName);

    LinkedTask completeTask(Long taskId, String result, String operatorName);

    void sendNotification(Long taskId, Integer notifyType, Long receiverId, String receiverName,
                          String title, String content, Integer level);
}
