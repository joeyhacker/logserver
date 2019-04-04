package com.inforefiner.cloud.log.service.flow;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
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
    private ElasticsearchTemplate elasticsearchTemplate;

    @Value("${flow_execution_log.index}")
    private String logIndex;

    @Value("${flow_execution_log.type}")
    private String logType;

    @Value("${flow_execution_log.index-date-format:yyyy-ww}")
    private String indexDateFormat;

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
            } while (elasticsearchTemplate.indexExists(index));
        } else {
            listIndex.add(logIndex);
        }
        String[] indexs = new String[listIndex.size()];
        return listIndex.toArray(indexs);
    }

    public void findLogByTypeAndEId(String type, String id, Pageable pageable, List<ExecutionDetailedLog> list, int page, int size) {
        String[] logIndexs = this.getLogsIndex();
        logger.info("get logIndexs {}", logIndexs);
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery().must(termQuery("contextMap.eid" +
                ".keyword", id)).must(termQuery("contextMap.etype.keyword", type)))
                .withIndices(logIndexs).withTypes(logType).withPageable(pageable).withSort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC)).build();
        List<ExecutionDetailedLog> detailedLogList = elasticsearchTemplate.queryForList(searchQuery, ExecutionDetailedLog.class);
        list.addAll(detailedLogList);
        if (detailedLogList != null && detailedLogList.size() >= 10000) {
            pageable = new PageRequest(++page, size);
            findLogByTypeAndEId(type, id, pageable, list, page, size);
        }
    }

    public ExecutionDetailedLogStatistics findLogByTypeAndEId(String type, String id) {
        Pageable pageable = new PageRequest(0, 10000);
        List<ExecutionDetailedLog> list = new ArrayList<>();
        findLogByTypeAndEId(type, id, pageable, list, 0, 10000);
        logger.info("get es query result {}", list.size());
        //获取统计信息
        return statisticsLogLevelCount(id, type, list);
    }

    private ExecutionDetailedLogStatistics statisticsLogLevelCount(String id, String type, List<ExecutionDetailedLog> list) {
        ExecutionDetailedLogStatistics executionDetailedLogStatistics = new ExecutionDetailedLogStatistics();
        executionDetailedLogStatistics.setList(list);
        String[] logIndexs = this.getLogsIndex();
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery().must(termQuery("contextMap.eid" +
                ".keyword", id)).must(termQuery("contextMap.etype.keyword", type)))
                .withIndices(logIndexs).withTypes(logType).addAggregation(AggregationBuilders.terms("group_name").field("level.keyword")).build();
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse searchResponse) {
                return searchResponse.getAggregations();
            }
        });
        List<Aggregation> levelList = aggregations.asList();
        if (levelList.size() == 1) {
            StringTerms sts = (StringTerms) levelList.get(0);
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
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(termQuery("contextMap.eid.keyword",
                executionId))
                .withIndices(logIndexs).withTypes(logType).addAggregation(AggregationBuilders.terms("etype_list").field("contextMap.etype.keyword").order(Terms.Order.count(true))).withSort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC)).build();
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse searchResponse) {
                return searchResponse.getAggregations();
            }
        });
        List<Aggregation> list = aggregations.asList();
        if (list.size() == 1) {
            StringTerms sts = (StringTerms) list.get(0);
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
                    SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery().must(termQuery(
                            "contextMap.eid.keyword", executionId)).must(termQuery("contextMap.etype.keyword", etype)).must(termQuery("level", "ERROR")))
                            .withIndices(logIndexs).withTypes(logType).withSort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC)).build();
                    List<ExecutionDetailedLog> detailedLogList = elasticsearchTemplate.queryForList(searchQuery, ExecutionDetailedLog.class);
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