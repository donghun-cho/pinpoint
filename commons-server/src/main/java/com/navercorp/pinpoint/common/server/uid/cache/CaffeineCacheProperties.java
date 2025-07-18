package com.navercorp.pinpoint.common.server.uid.cache;

import org.springframework.lang.Nullable;

import java.time.Duration;

//com.github.benmanes.caffeine.cache.CaffeineSpec
public class CaffeineCacheProperties {

    private int initialCapacity = -1;
    private long maximumSize = -1L;
    private boolean recordStats;

    @Nullable
    private Duration expireAfterWrite;

    @Nullable
    private Duration expireAfterAccess;

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public boolean isRecordStats() {
        return recordStats;
    }

    public void setRecordStats(boolean recordStats) {
        this.recordStats = recordStats;
    }

    public Duration getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(Duration expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public Duration getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(Duration expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }
}
