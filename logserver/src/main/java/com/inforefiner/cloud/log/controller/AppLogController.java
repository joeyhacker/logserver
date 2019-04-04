package com.inforefiner.cloud.log.controller;


import com.inforefiner.cloud.log.service.app.AppLogService;
import com.inforefiner.cloud.log.service.app.SyncTaskLog;
import com.inforefiner.cloud.log.service.flow.ExecutionDetailedLogStatistics;
import com.inforefiner.cloud.log.service.flow.ExecutionLogInfo;
import com.inforefiner.cloud.log.service.flow.FlowExecutionLogService;
import com.inforefiner.cloud.log.utils.JsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sync_task_log")
public class AppLogController {

    private Logger logger = LoggerFactory.getLogger(AppLogController.class);

    @Autowired
    private AppLogService appLogService;


    @ResponseBody
    @RequestMapping(value = "/{id}/{type}", method = RequestMethod.GET)
    public Map<String, Object> listTaskLogByType(@PathVariable("id") String taskId, @PathVariable("type") int logType,
                                                 @RequestParam("start") long start, @RequestParam("limit") int limit, @RequestParam("desc") boolean desc) {
        if (limit == 0) {
            limit = Integer.MAX_VALUE;
        }
        logger.info("taskId = {}, logType = {}, start = {}, limit = {}", taskId, logType, start, limit);
        Map<String, Object> res = appLogService.pagingSyncTaskLog(taskId, logType, start, limit, desc);
        return res;
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public void saveSyncTaskLog(@RequestBody String json) {
        SyncTaskLog syncTaskLog = JsonBuilder.getInstance().fromJson(json, SyncTaskLog.class);
        appLogService.saveSyncTaskLog(syncTaskLog);
    }
}
