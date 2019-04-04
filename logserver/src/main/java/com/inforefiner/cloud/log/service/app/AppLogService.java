//package com.inforefiner.cloud.log.service.app;
//
//
//import com.inforefiner.cloud.log.utils.JsonBuilder;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.RangeQueryBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
//import org.springframework.data.elasticsearch.core.query.IndexQuery;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
//import org.springframework.data.elasticsearch.core.query.SearchQuery;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import java.util.*;
//
//@Service
//public class AppLogService {
//
//    @Autowired
//    private ElasticsearchTemplate elasticsearchTemplate;
//
//    @PostConstruct
//    public void init() {
//        if (!elasticsearchTemplate.indexExists(SyncTaskLog.class)) {
//            elasticsearchTemplate.createIndex(SyncTaskLog.class);
//            elasticsearchTemplate.putMapping(SyncTaskLog.class);
//        }
//        if (!elasticsearchTemplate.indexExists(CollectorLog.class)) {
//            elasticsearchTemplate.createIndex(CollectorLog.class);
//            elasticsearchTemplate.putMapping(CollectorLog.class);
//        }
//    }
//
//    public void saveSyncTaskLog(SyncTaskLog syncTaskLog) {
//        IndexQuery indexQuery = new IndexQuery();
//        indexQuery.setObject(syncTaskLog);
//        indexQuery.setType("sync_task_log");
//        elasticsearchTemplate.index(indexQuery);
//    }
//
//    public Map<String, Object> pagingSyncTaskLog(String taskId, int logType, long start, int limit, boolean desc) {
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        boolQuery.must(QueryBuilders.termQuery("taskId", taskId));
//        boolQuery.must(QueryBuilders.termQuery("logType", logType));
//        if (start > -1) {
//            RangeQueryBuilder rqb = QueryBuilders.rangeQuery("logTime");
//            if (desc) {
//                boolQuery.must(rqb.lt(start));
//            } else {
//                boolQuery.must(rqb.gt(start));
//            }
//        }
//        SearchQuery searchQuery = new NativeSearchQuery(boolQuery);
//        searchQuery.addIndices("sync_task_log");
//        searchQuery.addTypes("sync_task_log");
//        Pageable pageable = new PageRequest(0, limit, desc ? Sort.Direction.DESC : Sort.Direction.ASC, "logTime");
//        searchQuery.setPageable(pageable);
//        List<SyncTaskLog> list = elasticsearchTemplate.queryForList(searchQuery, SyncTaskLog.class);
//        Map<String, Object> ret = new HashMap();
//        List<String> logs = new ArrayList();
//        if (list != null && list.size() > 0) {
//            long head = list.get(0).getLogTime();
//            long tail = list.get(list.size() - 1).getLogTime();
//            if (desc) {
//                ret.put("earliest", tail);
//                ret.put("latest", head);
//            } else {
//                ret.put("earliest", head);
//                ret.put("latest", tail);
//            }
//            for (SyncTaskLog log : list) {
//                logs.add(log.getLogText());
//            }
//            ret.put("list", logs);
//        } else {
//            ret.put("earliest", 0);
//            ret.put("latest", start);
//            ret.put("list", Collections.EMPTY_LIST);
//        }
//        return ret;
//    }
//}
