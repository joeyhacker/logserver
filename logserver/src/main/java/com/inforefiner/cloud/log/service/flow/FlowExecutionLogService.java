package com.inforefiner.cloud.log.service.flow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
//import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Service
public class FlowExecutionLogService {

    private static final Logger logger = LoggerFactory.getLogger(FlowExecutionLogService.class);

    @Autowired
    private TransportClient client;

    @Value("${flow_execution_log.index}")
    private String logIndex;

    @Value("${flow_execution_log.type}")
    private String logType;

    @Value("${flow_execution_log.index-date-format:yyyy-ww}")
    private String indexDateFormat;

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    //TODO 获取log4j2 index: metrics-yyyy-MM
    private String[] getLogsIndex() {
        List<String> listIndex = new ArrayList<>();
        if (StringUtils.isNotEmpty(indexDateFormat)) {
            SimpleDateFormat sfm = new SimpleDateFormat(indexDateFormat);
            Date date = new Date();
            String index = logIndex + "-" + sfm.format(date);
            do {
                listIndex.add(index);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                if ("yyyy-MM".equals(indexDateFormat) || "yyyyMM".equals(indexDateFormat)) {
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); //依次取前一个月
                } else if ("yyyy-ww".equals(indexDateFormat.toLowerCase()) || "yyyyww".equals(indexDateFormat.toLowerCase())) {
                    calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR) - 1); //依次取前一周
                }
                date = calendar.getTime();
                index = logIndex + "-" + sfm.format(date);
            } while (client.admin().indices().prepareExists(index).execute().actionGet().isExists());
        } else {
            listIndex.add(logIndex);
        }
        String[] indexs = new String[listIndex.size()];
        return listIndex.toArray(indexs);
    }

    public void findLogByTypeAndEId(String type, String id, List<ExecutionDetailedLog> list, int from, int size) {
        String[] logIndexs = this.getLogsIndex();
        logger.info("get logIndexs {}", logIndexs);
        QueryBuilder query = QueryBuilders.boolQuery().must(termQuery("contextMap.eid.keyword",id)).must(termQuery("contextMap.etype.keyword", type));
        SearchResponse response = client.prepareSearch(logIndexs).setTypes(logType).setQuery(query).addSort("@timestamp",SortOrder.ASC).setFrom(from).setSize(size).execute().actionGet();
        SearchHit[] hits = response.getHits().getHits();
        List<ExecutionDetailedLog> detailedLogList = new ArrayList<>();
        for (SearchHit hit : hits) {
            ExecutionDetailedLog e = gson.fromJson(gson.toJson(hit.getSource()), ExecutionDetailedLog.class);
            detailedLogList.add(e);
        }
        list.addAll(detailedLogList);
        if (detailedLogList != null && detailedLogList.size() >= 10000) {
            findLogByTypeAndEId(type, id, list, from+10000, size);
        }
    }

    public ExecutionDetailedLogStatistics findLogByTypeAndEId(String type, String id) {
        List<ExecutionDetailedLog> list = new ArrayList<>();
        findLogByTypeAndEId(type, id, list, 0, 10000);
        logger.info("get es query result {}", list.size());
        //获取统计信息
        return statisticsLogLevelCount(id, type, list);
    }

    private ExecutionDetailedLogStatistics statisticsLogLevelCount(String id, String type, List<ExecutionDetailedLog> list) {
        ExecutionDetailedLogStatistics executionDetailedLogStatistics = new ExecutionDetailedLogStatistics();
        executionDetailedLogStatistics.setList(list);
        String[] logIndexs = this.getLogsIndex();
        QueryBuilder query = QueryBuilders.boolQuery().must(termQuery("contextMap.eid.keyword",id)).must(termQuery("contextMap.etype.keyword", type));
        SearchResponse response = client.prepareSearch(logIndexs).setTypes(logType).setQuery(query).addAggregation(AggregationBuilders.terms("group_name").field("level.keyword")).addSort("@timestamp",SortOrder.ASC).execute().actionGet();
        Aggregations aggregations = response.getAggregations();
        Map<String, Aggregation> aggregationMap = aggregations.asMap();
        for (Map.Entry<String, Aggregation> map: aggregationMap.entrySet()) {
            StringTerms sts = (StringTerms) map.getValue();
            List<StringTerms.Bucket> buckets = sts.getBuckets();
            int sum = 0;
            if (buckets.size() > 0) {
                for (Terms.Bucket tbket : buckets) {
                    String levelName = (String) tbket.getKey();
                    Integer count = Integer.valueOf(tbket.getDocCount() + "");
                    if ("INFO".equals(levelName)) {
                        executionDetailedLogStatistics.setInfoCount(count);
                    } else if ("WARN".equals(levelName)) {
                        executionDetailedLogStatistics.setWarnCount(count);
                    } else if ("ERROR".equals(levelName)) {
                        executionDetailedLogStatistics.setWarnCount(count);
                    }
                    sum = sum + count;
                    logger.info("levelName:" + levelName + "  levelCount:" + count + "  sum:" + sum);
                }
                executionDetailedLogStatistics.setTotalCount(sum);
            }
        }
        logger.info("executionDetailedLogStatistics {} ", executionDetailedLogStatistics);
        return executionDetailedLogStatistics;
    }

    public List<String> listTypeByEid(String executionId) {
        String[] logIndexs = this.getLogsIndex();
        logger.info("es index logIndexs : {}", logIndexs);
        QueryBuilder query = QueryBuilders.boolQuery().must(termQuery("contextMap.eid.keyword",executionId));
        SearchResponse response = client.prepareSearch(logIndexs).setTypes(logType).setQuery(query).addAggregation(AggregationBuilders.terms("etype_list").field("contextMap.etype.keyword")).setFrom(0).setSize(500).addSort("@timestamp",SortOrder.ASC).execute().actionGet();
        Aggregations aggregations = response.getAggregations();
        Map<String, Aggregation> aggregationMap = aggregations.asMap();
        for (Map.Entry<String, Aggregation> map: aggregationMap.entrySet()) {
            StringTerms sts = (StringTerms) map.getValue();
            List<StringTerms.Bucket> buckets = sts.getBuckets();
            if (buckets.size() > 0) {
                return buckets.stream().map(b -> b.getKeyAsString()).collect(Collectors.toList());
            }
        }
        return null;
    }

    public ExecutionLogInfo mapTypeByEid(String executionId, int offset, int limit) {
        ExecutionLogInfo executionLogInfo = new ExecutionLogInfo();
        Map<String, String> map = new TreeMap<>();
        logger.info("start list");
        List<String> list = listTypeByEid(executionId);
        logger.info("end list {}", list);
        if (list != null) {
            String[] logIndexs = this.getLogsIndex();
            logger.info("list es index logIndexs : {}", logIndexs);
            int count = 0;
            for (String etype : list) {
                if (count >= offset) {
                    QueryBuilder query = QueryBuilders.boolQuery().must(termQuery("contextMap.eid.keyword",executionId)).must(termQuery("contextMap.etype.keyword", etype)).must(termQuery("level.keyword", "ERROR"));
                    SearchResponse response = client.prepareSearch(logIndexs).setTypes(logType).setQuery(query).setFrom(0).setSize(10000).addSort("@timestamp",SortOrder.ASC).execute().actionGet();

                    SearchHit[] hits = response.getHits().getHits();
                    List<ExecutionDetailedLog> detailedLogList = new ArrayList<>();
                    for (SearchHit hit : hits) {
                        ExecutionDetailedLog e = gson.fromJson(gson.toJson(hit.getSource()), ExecutionDetailedLog.class);
                        detailedLogList.add(e);
                    }
                    if (detailedLogList != null && detailedLogList.size() > 0) {
                        map.put(etype, "ERROR");
                    } else {
                        map.put(etype, "INFO");
                    }
                }
                count++;
                if (count >= limit + offset) {
                    break;
                }
            }
            executionLogInfo.setLogInfo(map);
            executionLogInfo.setTotalSize(list.size());
        }
        logger.info("return result {}", executionLogInfo);
        return executionLogInfo;
    }
}