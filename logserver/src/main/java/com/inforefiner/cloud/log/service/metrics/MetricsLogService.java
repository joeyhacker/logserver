package com.inforefiner.cloud.log.service.metrics;

import com.inforefiner.cloud.log.service.flow.ExecutionDetailedLog;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class MetricsLogService {

    @Value("${woven.management.metrics.export.elastic.index:metrics}")
    private String metricsIndex;

    @Value("${woven.management.metrics.export.elastic.index-date-format:yyyy-MM}")
    private String indexDateFormat;

    @Value("${woven.management.metrics.export.elastic.time-zone:GMT+8}")
    public String timeZone;

    @Autowired
    private TransportClient client;

    private static final String GROUP_NAME = "groupBy";


    //TODO 获取metrics index: metrics-yyyy-MM
    private String[] getMetricsIndex() {
        SimpleDateFormat sfm = new SimpleDateFormat(indexDateFormat);
        List<String> listIndex = new ArrayList<>();
        Date date = new Date();
        String index = metricsIndex + "-" + sfm.format(date);
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
            index = metricsIndex + "-" + sfm.format(date);
        } while (client.admin().indices().prepareExists(index).execute().actionGet().isExists());

        String[] indexs = new String[listIndex.size()];
        return listIndex.toArray(indexs);
    }

    /**
     * 根据时间分组统计url访问量
     *
     * @param startDate 开始时间(包含)
     * @param endDate   结束时间(不包含)
     * @return
     */
    public List<Map<String, Object>> allRequestCount(String startDate, String endDate, String group, String dataFormat) {
        String[] indexs = getMetricsIndex();
//        BoolQueryBuilder boolQueryBuilder = boolQuery();
//        boolQueryBuilder.must(termQuery("name", "http_server_requests"));
//        boolQueryBuilder.must(rangeQuery("@timestamp").gt(startDate).lt(endDate).includeLower(true).includeUpper(false));
        DateHistogramAggregationBuilder dhAgg =
                AggregationBuilders.dateHistogram(GROUP_NAME).field("@timestamp").timeZone(DateTimeZone.forID("Asia/Shanghai"));
        if ("day".equals(group) || StringUtils.isEmpty(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.DAY).format(dataFormat);
        } else if ("week".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.WEEK).format(dataFormat);
        } else if ("hour".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.HOUR).format(dataFormat);
        } else if ("minute".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.MINUTE).format(dataFormat);
        } else if ("month".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.MONTH).format(dataFormat);
        } else {
            throw new IllegalArgumentException("can't support group Type:" + group);
        }
        QueryBuilder query = QueryBuilders.boolQuery().must(termQuery("name", "http_server_requests")).must(rangeQuery("@timestamp").gt(startDate).lt(endDate).includeLower(true).includeUpper(false));
        SearchResponse response = client.prepareSearch(indexs).setTypes("doc").setQuery(query).addAggregation(dhAgg).addSort("@timestamp",SortOrder.ASC).execute().actionGet();
        Aggregations aggregations = response.getAggregations();
        Histogram agg = (Histogram)  aggregations.asMap().get(GROUP_NAME);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", entry.getKey().toString());
            map.put("key_as_string", entry.getKeyAsString());
            map.put("count", entry.getDocCount());
            list.add(map);
        }
        return list;
    }

    /**
     * 按时间分组统计成功的请求数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    private List<Map<String, Object>> successRequestCount(String startDate, String endDate, String group, String dataFormat) {
        String[] indexs = getMetricsIndex();
        DateHistogramAggregationBuilder dhAgg =
                AggregationBuilders.dateHistogram(GROUP_NAME).field("@timestamp").timeZone(DateTimeZone.forID("Asia/Shanghai"));
        if ("day".equals(group) || StringUtils.isEmpty(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.DAY).format(dataFormat);
        } else if ("week".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.WEEK).format(dataFormat);
        } else if ("hour".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.HOUR).format(dataFormat);
        } else if ("minute".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.MINUTE).format(dataFormat);
        } else if ("month".equals(group)) {
            dhAgg.dateHistogramInterval(DateHistogramInterval.MONTH).format(dataFormat);
        } else {
            throw new IllegalArgumentException("can't support group Type:" + group);
        }
        QueryBuilder query = QueryBuilders.boolQuery().must(termQuery("name", "http_server_requests")).must(queryStringQuery("SUCCESS").field("outcome")).must(rangeQuery("@timestamp").gt(startDate).lt(endDate).includeLower(true).includeUpper(false));
        SearchResponse response = client.prepareSearch(indexs).setTypes("doc").setQuery(query).addAggregation(dhAgg).addSort("@timestamp",SortOrder.ASC).execute().actionGet();
        Aggregations aggregations = response.getAggregations();
        Histogram agg = (Histogram)  aggregations.asMap().get(GROUP_NAME);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Histogram.Bucket entry : agg.getBuckets()) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", entry.getKey().toString());
            map.put("key_as_string", entry.getKeyAsString());
            map.put("count", entry.getDocCount());
            list.add(map);
        }
        return list;
    }

    /**
     * 按时间段分组统计url请求的成功率和失败率
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Map<String, Object>> findRequestSuccessRate(String startDate, String endDate, String group, String dataFormat) {
        List<Map<String, Object>> all_count = this.allRequestCount(startDate, endDate, group, dataFormat);
        List<Map<String, Object>> succe_count = this.successRequestCount(startDate, endDate, group, dataFormat);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> acMap : all_count) {
            Map<String, Object> map = new HashMap<>();
            boolean find_succ = false;
            String key_as_string = acMap.get("key_as_string").toString();
            map.put("key_as_string", key_as_string);
            map.put("key", acMap.get("key"));
            for (Map<String, Object> scMap : succe_count) {
                find_succ = key_as_string.equals(scMap.get("key_as_string").toString());
                if (find_succ) {
                    long total = (long) acMap.get("count");
                    long success = (long) scMap.get("count");
                    double success_rate = (success * 1.0) / (total * 1.0);
                    long fail = total - success;
                    double fail_rate = (fail * 1.0) / (total * 1.0);
                    map.put("success", success);
                    map.put("success_rate", success_rate);
                    map.put("fail", fail);
                    map.put("fail_rate", fail_rate);
                    break;
                }
            }
            if (!find_succ) {
                map.put("success_rate", 0);
                map.put("fail", acMap.get("count"));
                map.put("fail_rate", 1);
            }
            result.add(map);
        }
        return result;
    }

    /**
     * 查看耗时最长的Top limit request地址
     *
     * @param startDate
     * @param endDate
     * @param limit
     * @return
     */
    public Object topTimmerRequest(String startDate, String endDate, int limit) {
        String[] indexs = getMetricsIndex();
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(termQuery("name", "http_server_requests"));
        queryBuilder.must(rangeQuery("@timestamp").gt(startDate).lt(endDate).includeLower(true).includeUpper(false));
        SortBuilder sort = SortBuilders.fieldSort("max")   //排序字段
                .order(SortOrder.DESC);

        SearchResponse response = client.prepareSearch(indexs).setTypes("doc").setQuery(queryBuilder).addSort(sort).setFrom(0).setSize(limit).execute().actionGet();
        SearchHit[] hits = response.getHits().getHits();
        List<Map> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            list.add(hit.getSource());
        }
        return list;
    }


    /**
     * 统计访问流量最大的top limit请求地址
     *
     * @param startDate
     * @param endDate
     * @param limit
     * @return
     */
    public List<Map<String, Object>> topRequest(String startDate, String endDate, int limit) {
        String[] indexs = getMetricsIndex();
        BoolQueryBuilder queryBuilder = boolQuery();
        queryBuilder.must(rangeQuery("@timestamp").gt(startDate).lt(endDate).includeLower(true).includeUpper(false));
        queryBuilder.must(termQuery("name", "http_server_requests"));
        TermsAggregationBuilder teamAgg = AggregationBuilders.terms("uri_group").field("uri");
        SearchResponse response = client.prepareSearch(indexs).setTypes("doc").setQuery(queryBuilder).addAggregation(teamAgg).setFrom(0).setSize(limit).execute().actionGet();
        Aggregations aggregations = response.getAggregations();
        List<Aggregation> list = aggregations.asList();
        List<Map<String, Object>> result = new ArrayList<>();
        if (list.size() == 1) {
            StringTerms sts = (StringTerms) list.get(0);
            List<StringTerms.Bucket> buckets = sts.getBuckets();
            if (buckets.size() > 0) {
                for (int i = 0; i < buckets.size(); i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("uri", buckets.get(i).getKeyAsString());
                    map.put("count", buckets.get(i).getDocCount());
                    map.put("key", buckets.get(i).getKey());
                    map.put("aggre", buckets.get(i).getAggregations());
                    result.add(map);
                }
            }
        }
        return result;
    }

}
