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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Data class to hold statistics about cache usage
 * @author Visural
 */
public class CacheStats implements Serializable {
    
    public final AtomicLong hitCount;
    public final AtomicLong missCount;
    public final AtomicLong loadCount;
    public final AtomicLong totalLoadTime;
    public final AtomicLong evictionCount;

    public CacheStats() {
        this(0, 0, 0, 0, 0);
    }

    public CacheStats(long hitCount, long missCount, long loadCount, long totalLoadTime, long evictionCount) {
        this.hitCount = new AtomicLong(hitCount);
        this.missCount = new AtomicLong(missCount);
        this.loadCount = new AtomicLong(loadCount);
        this.totalLoadTime = new AtomicLong(totalLoadTime);
        this.evictionCount = new AtomicLong(evictionCount);    
    }
    
    public CacheStats plus(CacheStats other) {
        return new CacheStats(hitCount.get()+other.hitCount.get(),
                missCount.get()+other.missCount.get(),
                loadCount.get()+other.loadCount.get(),
                totalLoadTime.get()+other.totalLoadTime.get(),
                evictionCount.get()+other.evictionCount.get());
    }
    
    public CacheStats minus(CacheStats other) {
        return new CacheStats(hitCount.get()-other.hitCount.get(),
                missCount.get()-other.missCount.get(),
                loadCount.get()-other.loadCount.get(),
                totalLoadTime.get()-other.totalLoadTime.get(),
                evictionCount.get()-other.evictionCount.get());
    }
    
    public long getRequestCount() {
        return hitCount.get()+missCount.get();
    }
    
    public double getMissRate() {
        return (double)missCount.get()/((double)missCount.get()+(double)hitCount.get());
    }
    
    public double getHitRate() {
        return (double)hitCount.get()/((double)missCount.get()+(double)hitCount.get());
    }
    
    public long averageLoadTimeNanos() {
        return totalLoadTime.get() / loadCount.get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("hitCount = ").append(hitCount).append('\n');
        sb.append("missCount = ").append(missCount).append('\n');
        sb.append("requestCount = ").append(hitCount.get()+missCount.get()).append('\n');
        sb.append("loadCount = ").append(loadCount).append('\n');
        sb.append("totalLoadTime = ").append(totalLoadTime).append('\n');
        sb.append("averageLoadTime = ").append(averageLoadTimeNanos()).append('\n');
        sb.append("evictionCount = ").append(evictionCount).append('\n');
        return sb.toString();
    }
        
}
