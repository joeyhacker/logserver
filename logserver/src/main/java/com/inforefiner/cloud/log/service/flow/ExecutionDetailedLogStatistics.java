package com.inforefiner.cloud.log.service.flow;

import java.util.List;

public class ExecutionDetailedLogStatistics {

    private List<ExecutionDetailedLog> list ;

    private int totalCount;

    private int infoCount;

    private int warnCount;

    private int errorCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getInfoCount() {
        return infoCount;
    }

    public void setInfoCount(int infoCount) {
        this.infoCount = infoCount;
    }

    public int getWarnCount() {
        return warnCount;
    }

    public void setWarnCount(int warnCount) {
        this.warnCount = warnCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<ExecutionDetailedLog> getList() {
        return list;
    }

    public void setList(List<ExecutionDetailedLog> list) {
        this.list = list;
    }
}
