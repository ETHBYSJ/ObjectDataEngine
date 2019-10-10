package com.sjtu.objectdataengine.utils;

import java.util.concurrent.TimeUnit;

public class ExpireEnum {
    private long time;
    private TimeUnit timeUnit;

    ExpireEnum(long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
