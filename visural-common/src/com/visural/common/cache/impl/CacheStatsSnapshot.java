/*
 *  Copyright 2010 Richard Nichols.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.visural.common.cache.impl;

import com.visural.common.StringUtil;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Data class to hold statistics about cache usage
 * @author Visural
 */
public class CacheStatsSnapshot implements Serializable {
    
    private final AtomicLong hitCount;
    private final AtomicLong missCount;
    private final AtomicLong loadCount;
    private final AtomicLong totalLoadTime;
    private final AtomicLong evictionCount;
    private final int entries;
    private final int estimatedMemoryUsage;
       
    public CacheStatsSnapshot(CacheStats stats, int entries, int estimatedMemoryUsage) {
        this(stats.getHitCount().get(), stats.getMissCount().get(), stats.getLoadCount().get(), stats.getTotalLoadTime().get(), stats.getEvictionCount().get(), entries, estimatedMemoryUsage);        
    }

    public CacheStatsSnapshot(long hitCount, long missCount, long loadCount, long totalLoadTime, long evictionCount, int entries, int estimatedMemoryUsage) {
        this.hitCount = new AtomicLong(hitCount);
        this.missCount = new AtomicLong(missCount);
        this.loadCount = new AtomicLong(loadCount);
        this.totalLoadTime = new AtomicLong(totalLoadTime);
        this.evictionCount = new AtomicLong(evictionCount);    
        this.entries = entries;
        this.estimatedMemoryUsage = estimatedMemoryUsage;
    }
    
    public CacheStatsSnapshot plus(CacheStatsSnapshot other) {
        return new CacheStatsSnapshot(hitCount.get()+other.hitCount.get(),
                missCount.get()+other.missCount.get(),
                loadCount.get()+other.loadCount.get(),
                totalLoadTime.get()+other.totalLoadTime.get(),
                evictionCount.get()+other.evictionCount.get(),
                entries+other.entries,
                estimatedMemoryUsage+other.estimatedMemoryUsage);
    }
    
    public CacheStatsSnapshot minus(CacheStatsSnapshot other) {
        return new CacheStatsSnapshot(hitCount.get()-other.hitCount.get(),
                missCount.get()-other.missCount.get(),
                loadCount.get()-other.loadCount.get(),
                totalLoadTime.get()-other.totalLoadTime.get(),
                evictionCount.get()-other.evictionCount.get(),
                entries-other.entries,
                estimatedMemoryUsage-other.estimatedMemoryUsage);
    }

    public int getEntries() {
        return entries;
    }
    
    public long getRequestCount() {
        return hitCount.get()+missCount.get();
    }
    
    public double getMissRate() {
        return (double)missCount.get()/((double)missCount.get()+(double)hitCount.get());
    }
    
    public String getMissRatePercent() {
        return StringUtil.formatDecimal(getMissRate()*100, 2)+"%";
    }
    
    public double getHitRate() {
        return (double)hitCount.get()/((double)missCount.get()+(double)hitCount.get());
    }
    
    public String getHitRatePercent() {
        return StringUtil.formatDecimal(getHitRate()*100, 2)+"%";
    }
    
    public long getAverageLoadTimeNanos() {
        return totalLoadTime.get() / loadCount.get();
    }

    public AtomicLong getEvictionCount() {
        return evictionCount;
    }

    public AtomicLong getHitCount() {
        return hitCount;
    }

    public AtomicLong getLoadCount() {
        return loadCount;
    }

    public AtomicLong getMissCount() {
        return missCount;
    }

    public AtomicLong getTotalLoadTime() {
        return totalLoadTime;
    }

    public int getEstimatedMemoryUsageBytes() {
        return estimatedMemoryUsage;
    }

    public String getEstimatedMemoryUsageMB() {
        return StringUtil.formatDecimal((double)estimatedMemoryUsage/(1024d*1024d), 2)+" MB";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("hitCount = ").append(hitCount).append('\n');
        sb.append("missCount = ").append(missCount).append('\n');
        sb.append("requestCount = ").append(hitCount.get()+missCount.get()).append('\n');
        sb.append("loadCount = ").append(loadCount).append('\n');
        sb.append("totalLoadTime = ").append(totalLoadTime).append('\n');
        sb.append("averageLoadTime = ").append(getAverageLoadTimeNanos()).append('\n');
        sb.append("evictionCount = ").append(evictionCount).append('\n');
        sb.append("estimatedMemoryUsage = ").append(estimatedMemoryUsage).append("\n");
        return sb.toString();
    }
        
}
