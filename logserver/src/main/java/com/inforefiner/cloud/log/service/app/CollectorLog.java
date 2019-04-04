package com.inforefiner.cloud.log.service.app;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "collector_log", type = "collector_log")
public class CollectorLog {

    @Id
    private String id;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.String, store = true)
    private String collectorId;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.Integer, store = true)
    private int logType;

    @Field(index = FieldIndex.no, type = FieldType.String, store = false)
    private String logText;

    @Field(index = FieldIndex.not_analyzed, type = FieldType.Long, store = true)
    private long logTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(String collectorId) {
        this.collectorId = collectorId;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }
}
