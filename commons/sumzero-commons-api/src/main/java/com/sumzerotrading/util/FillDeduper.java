package com.sumzerotrading.util;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class FillDeduper {

    protected long maxSize = 200_000;
    protected Duration expiration = Duration.ofMinutes(30);

    protected Cache<String, Boolean> seen;

    public FillDeduper() {
        initializeCache();
    }

    public FillDeduper(long maxSize, Duration expiration) {
        this.maxSize = maxSize;
        this.expiration = expiration;
        initializeCache();
    }

    protected void initializeCache() {
        seen = Caffeine.newBuilder().expireAfterWrite(expiration).maximumSize(maxSize).build();
    }

    public boolean firstTime(String fillId) {
        return seen.asMap().putIfAbsent(fillId, Boolean.TRUE) == null;
    }

}
