package com.inforefiner.cloud.log.service.app;

//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.*;

//@Document(indexName = "sync_task_log", type = "sync_task_log")
public class SyncTaskLog {

//    @Id
    private String id;

//    @Field(index = FieldIndex.no, type = FieldType.String, store = false)
    private String tenant;

//    @Field(index = FieldIndex.no, type = FieldType.String, store = false)
    private String user;

//    @Field(index = FieldIndex.not_analyzed, type = FieldType.String, store = true)
    private String taskId;

//    @Field(index = FieldIndex.not_analyzed, type = FieldType.Integer, store = true)
    private int logType;

//    @Field(index = FieldIndex.no, type = FieldType.String, store = false)
    private String logText;

//    @Field(index = FieldIndex.not_analyzed, type = FieldType.Long, store = true)
    private long logTime;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
