package com.inforefiner.cloud.log.service.flow;

public class Instant {

    private long epochSecond;

    private long nanoOfSecond;

    public long getEpochSecond() {
        return epochSecond;
    }

    public void setEpochSecond(long epochSecond) {
        this.epochSecond = epochSecond;
    }

    public long getNanoOfSecond() {
        return nanoOfSecond;
    }

    public void setNanoOfSecond(long nanoOfSecond) {
        this.nanoOfSecond = nanoOfSecond;
    }
}
