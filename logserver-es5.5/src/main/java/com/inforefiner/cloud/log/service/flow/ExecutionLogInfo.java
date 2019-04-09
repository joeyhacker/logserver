package com.inforefiner.cloud.log.service.flow;

import java.io.Serializable;
import java.util.Map;

public class ExecutionLogInfo implements Serializable {

    private Map<String,String> logInfo;

    private int totalSize;

    public Map<String, String> getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(Map<String, String> logInfo) {
        this.logInfo = logInfo;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public String toString() {
        return "ExecutionLogInfo{" +
                "logInfo=" + logInfo +
                ", totalSize=" + totalSize +
                '}';
    }
}
