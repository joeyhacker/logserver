package com.inforefiner.cloud.log.controller;


import com.inforefiner.cloud.log.service.flow.FlowExecutionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flow_execution_log")
public class FlowExecutionLogController {

    private Logger logger = LoggerFactory.getLogger(com.inforefiner.cloud.log.controller.FlowExecutionLogController.class);

    @Autowired
    private FlowExecutionLogService flowExecutionLogService;

//    @ResponseBody
//    @RequestMapping(value = "/{eid}/{offset}/{limit}", method = RequestMethod.GET)
//    public ExecutionLogInfo getLogTypes(@PathVariable("eid") String executionId, @PathVariable("offset") int offset, @PathVariable("limit") int limit) {
//        logger.info("executionId {} offset {} limit {}", executionId, offset, limit);
//        return flowExecutionLogService.mapTypeByEid(executionId, offset, limit);
//    }
//
//    @ResponseBody
//    @RequestMapping(value = "/{eid}/{logType}", method = RequestMethod.GET)
//    public ExecutionDetailedLogStatistics listLogWithEidAndType(@PathVariable("eid") String executionId, @PathVariable("logType") String logType) {
//        logger.info("executionId {} logType {} ",executionId,logType);
//        return flowExecutionLogService.findLogByTypeAndEId(logType, executionId);
//    }

}
