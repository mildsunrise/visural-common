package com.visural.common.cache.impl;

import com.visural.common.cache.Cache;
import com.visural.common.cache.EvictionStrategy;
import java.io.Serializable;

public class CacheSettings implements Serializable {

    private int timeToLive;
    private int maxEntries;
    private EvictionStrategy evictionStrategy;   
    private boolean softValues;   
    private boolean singletonCache;

    public CacheSettings(Cache settings) {
        timeToLive = settings.timeToLive();
        maxEntries = settings.maxEntries();
        evictionStrategy = settings.evictionStrategy();
        softValues = settings.softValues();
        singletonCache = settings.singletonCache();
    }

    public EvictionStrategy getEvictionStrategy() {
        return evictionStrategy;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public boolean isSingletonCache() {
        return singletonCache;
    }

    public boolean isSoftValues() {
        return softValues;
    }
    
}
