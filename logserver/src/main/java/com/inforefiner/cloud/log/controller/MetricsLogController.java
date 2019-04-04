package com.inforefiner.cloud.log.controller;


import com.inforefiner.cloud.log.service.metrics.MetricsLogService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/metrics")
public class MetricsLogController {

    private Logger logger = LoggerFactory.getLogger(MetricsLogController.class);

    @Autowired
    private MetricsLogService metricsLogService;


    @ResponseBody
    @RequestMapping(value = "/request/count", method = RequestMethod.GET)
    public Object getRequestCount(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, @RequestParam("group") String group, @RequestParam("dateFormat") String dateFormat) {
        if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) {
            throw new IllegalArgumentException("QueryParam startDate or endDate can't be null");
        }
        if (StringUtils.isEmpty(dateFormat) && StringUtils.isEmpty(group)) {
            dateFormat = "yyyy-MM-dd";
            group = "day";
        } else if ((StringUtils.isEmpty(dateFormat) && StringUtils.isNotEmpty(group)) || (StringUtils.isNotEmpty(dateFormat) && StringUtils.isEmpty(group))) {
            throw new IllegalArgumentException("QueryParam group or dataFormat must at the same time is empty or not at the same time is empty");
        }
        startDate = convertTimeZoneCST_To_GMT(startDate, "GMT");
        endDate = convertTimeZoneCST_To_GMT(endDate, "GMT");
        return metricsLogService.allRequestCount(startDate, endDate, group, dateFormat);
    }

    @ResponseBody
    @RequestMapping(value = "/request/success_count", method = RequestMethod.GET)
    public Object getSuccessRequestCount(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, @RequestParam("group") String group, @RequestParam("dateFormat") String dateFormat) {
        if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) {
            throw new IllegalArgumentException("QueryParam startDate or endDate can't be null");
        }
        if (StringUtils.isEmpty(group) && StringUtils.isEmpty(dateFormat)) {
            group = "day";
            dateFormat = "yyyy-MM-dd";
        } else if ((StringUtils.isEmpty(dateFormat) && StringUtils.isNotEmpty(group)) || (StringUtils.isNotEmpty(dateFormat) && StringUtils.isEmpty(group))) {
            throw new IllegalArgumentException("QueryParam group or dataFormat must at the same time is empty or not at the same time is empty");
        }
        startDate = convertTimeZoneCST_To_GMT(startDate, "GMT");
        endDate = convertTimeZoneCST_To_GMT(endDate, "GMT");
        return metricsLogService.findRequestSuccessRate(startDate, endDate, group, dateFormat);
    }

    @ResponseBody
    @RequestMapping(value = "/request/top", method = RequestMethod.GET)
    public List<Map<String, Object>> getTopRequest(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, @RequestParam("limit") int limit) {
        if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) {
            throw new IllegalArgumentException("QueryParam startDate or endDate can't be null");
        }
        if (limit < 0) {
            limit = 100;
        }
        startDate = convertTimeZoneCST_To_GMT(startDate, metricsLogService.timeZone);
        endDate = convertTimeZoneCST_To_GMT(endDate, metricsLogService.timeZone);
        List<Map<String, Object>> result = metricsLogService.topRequest(startDate, endDate, limit);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/request/toptimmer", method = RequestMethod.GET)
    public Object getTopTimmerRequest(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate, @RequestParam("limit") int limit) {
        if (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) {
            throw new IllegalArgumentException("QueryParam startDate or endDate can't be null");
        }
        if (limit < 0) {
            limit = 100;
        }
        startDate = convertTimeZoneCST_To_GMT(startDate, metricsLogService.timeZone);
        endDate = convertTimeZoneCST_To_GMT(endDate, metricsLogService.timeZone);
        Object ret = metricsLogService.topTimmerRequest(startDate, endDate, limit);
        return ret;
    }

    private String convertTimeZoneCST_To_GMT(String time, String timeZone) {
        SimpleDateFormat cstDf = new SimpleDateFormat("yyyy-MM-dd");
//		cstDf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        SimpleDateFormat gmtDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        gmtDf.setTimeZone(TimeZone.getTimeZone(timeZone));
        Calendar calendar = new GregorianCalendar();
        try {
            calendar.setTime(cstDf.parse(time));
            time = gmtDf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}
