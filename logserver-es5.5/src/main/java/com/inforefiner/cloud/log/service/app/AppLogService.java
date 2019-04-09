package com.inforefiner.cloud.log.service.app;


import com.inforefiner.cloud.log.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class AppLogService {

    private Logger logger = LoggerFactory.getLogger(com.inforefiner.cloud.log.service.app.AppLogService.class);

    private static final String SYNC_TASK_LOG_INDEX = "sync_task_log";

    private static final String SYNC_TASK_LOG_INDEX_TYPE = "sync_task_log";

    @Autowired
    private Client client;

    @Value("${sync_task_log.mapping.path:}")
    private String syncTaskLogMappingPath;

    @PostConstruct
    public void init() {
        IndicesExistsResponse response = client.admin().indices().prepareExists(SYNC_TASK_LOG_INDEX).get();
        if (!response.isExists()) {
            String mapping = null;
            if (StringUtils.isBlank(syncTaskLogMappingPath)) {
                mapping = FileUtil.loadFromClassPath("/mapping/sync_task_log_mapping.json");
            } else {
                mapping = FileUtil.load(syncTaskLogMappingPath);
            }
            client.admin().indices().prepareCreate(SYNC_TASK_LOG_INDEX).addMapping(SYNC_TASK_LOG_INDEX_TYPE, mapping).get();
        }
    }

    public void saveSyncTaskLog(Map syncTaskLog) {
        IndexResponse response =
                client.prepareIndex(SYNC_TASK_LOG_INDEX, SYNC_TASK_LOG_INDEX_TYPE).setSource(syncTaskLog).get();
        if (response.status().getStatus() != 201) {
            logger.error("index create error: " + response.status().toString());
        }
    }

    public Map<String, Object> pagingSyncTaskLog(String taskId, int logType, long start, int limit, boolean desc) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("taskId", taskId));
        boolQuery.must(QueryBuilders.termQuery("logType", logType));
        if (start > -1) {
            RangeQueryBuilder rqb = QueryBuilders.rangeQuery("logTime");
            if (desc) {
                boolQuery.must(rqb.lt(start));
            } else {
                boolQuery.must(rqb.gt(start));
            }
        }
        SearchResponse response =
                client.prepareSearch(SYNC_TASK_LOG_INDEX).setTypes(SYNC_TASK_LOG_INDEX_TYPE).setQuery(boolQuery).setFrom(0).setSize(limit).addSort("logTime", desc ? SortOrder.DESC : SortOrder.ASC).get();
        Map<String, Object> ret = new HashMap();
        List<String> logs = new ArrayList();
        SearchHit[] hits = response.getHits().getHits();
        if (hits != null && hits.length > 0) {
            long head = (long) hits[0].getSource().get("logTime");
            long tail = (long) hits[hits.length - 1].getSource().get("logTime");
            if (desc) {
                ret.put("earliest", tail);
                ret.put("latest", head);
            } else {
                ret.put("earliest", head);
                ret.put("latest", tail);
            }
            for (SearchHit hit : hits) {
                logs.add((String) hit.getSource().get("logText"));
            }
            ret.put("list", logs);
        } else {
            ret.put("earliest", 0);
            ret.put("latest", start);
            ret.put("list", Collections.EMPTY_LIST);
        }
        return ret;
    }
}
