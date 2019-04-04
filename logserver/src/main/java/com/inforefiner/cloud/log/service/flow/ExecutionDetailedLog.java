package com.inforefiner.cloud.log.service.flow;

public class ExecutionDetailedLog {

    private String _id;

    private long timestamp;

    private String level;

    private String message;

    private String loggerName;

    private String priority;

    private String pid;

   private ContextMap contextMap;

   private Instant instant;

    private String thread;

    private String path;

    private String stack_trace;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public long getTimestamp() {
        return instant.getEpochSecond()*1000;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return level;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }



    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public ContextMap getContextMap() {
        return contextMap;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public void setContextMap(ContextMap contextMap) {
        this.contextMap = contextMap;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStack_trace() {
        return stack_trace;
    }

    public void setStack_trace(String stack_trace) {
        this.stack_trace = stack_trace;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public String toString() {
        return "ExecutionDetailedLog{" +
                "_id='" + _id + '\'' +
                ", timestamp=" + timestamp +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", loggerName='" + loggerName + '\'' +
                ", priority='" + priority + '\'' +
                ", pid='" + pid + '\'' +
                ", contextMap=" + contextMap +
                ", instant=" + instant +
                ", thread='" + thread + '\'' +
                ", path='" + path + '\'' +
                ", stack_trace='" + stack_trace + '\'' +
                '}';
    }
}
