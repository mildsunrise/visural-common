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

import com.visural.common.cache.Cache;
import com.visural.common.cache.EvictionStrategy;
import com.visural.common.cache.KeyProvider;
import com.visural.common.cache.MethodCall;
import com.visural.common.datastruct.LRUCache;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class MethodCache {

    private final Cache settings;
    private Map<String, CacheEntry> cache;
    private PriorityQueue<CacheEntry> sortedEntries = null;
    private final KeyProvider kp;

    public MethodCache(Cache settings, Method m, KeyProvider kp) {
        this.settings = settings;        
        if (settings.maxEntries() <= 0) {
            throw new IllegalArgumentException(String.format("Method '%s' has @Cache with maxEntries <= 0", m));            
        }
        switch (settings.evictionStrategy()) {
            case FIFO:
                sortedEntries = new PriorityQueue<CacheEntry>(11, new Comparator<CacheEntry>() {
                    public int compare(CacheEntry o1, CacheEntry o2) {
                        return Long.valueOf(o1.getCreated()).compareTo(o2.getCreated());
                    }
                }); 
                cache = new HashMap();
                break;
            case LFU:
                sortedEntries = new PriorityQueue<CacheEntry>(11, new Comparator<CacheEntry>() {
                    public int compare(CacheEntry o1, CacheEntry o2) {
                        int result = Integer.valueOf(o1.getUses()).compareTo(o2.getUses());
                        if (result == 0) {
                            // fallback to FIFO for same match
                            result = Long.valueOf(o1.getCreated()).compareTo(o2.getCreated());
                        } 
                        return result;
                    }
                }); 
                cache = new HashMap();
                break;
            case LFU_TIMECOST:
                sortedEntries = new PriorityQueue<CacheEntry>(11, new Comparator<CacheEntry>() {
                    public int compare(CacheEntry o1, CacheEntry o2) {
                        int result = Integer.valueOf(o1.getUsesByTimecost()).compareTo(o2.getUsesByTimecost());
                        if (result == 0) {
                            // fallback to FIFO for same match
                            result = Long.valueOf(o1.getCreated()).compareTo(o2.getCreated());
                        } 
                        return result;
                    }
                }); 
                cache = new HashMap();
                break;
            case LRU:
                cache = new LRUCache<String, CacheEntry>(settings.maxEntries());
                break;
            default:
                throw new IllegalStateException("Should not happen.");
        }
        this.kp = kp;
    }

    public Cache getSettings() {
        return settings;
    }
    
    public CacheEntry get(MethodCall mc) {
        String key = kp.getKey(mc);
        CacheEntry c = cache.get(key);
        if (c != null) {
            if (c.isExpired()) {
                invalidateCache(key);
                c = null;                
            } else {
                if (sortedEntries != null && !settings.evictionStrategy().equals(EvictionStrategy.FIFO)) {
                    synchronized(this) {
                        c.incrementUses();
                        sortedEntries.remove(c);
                        sortedEntries.add(c);
                    }
                } else {
                    c.incrementUses();
                }                
            }
        }
        return c;
    }
    
    public synchronized void put(long created, long timeCost, MethodCall methodCall, Object result) {
        String key = kp.getKey(methodCall);
        CacheEntry e = settings.softValues() ? 
                new CacheEntry(key, created, settings.timeToLive(), timeCost, new SoftReference(result)) :
                new CacheEntry(key, created, settings.timeToLive(), timeCost, result);
        cache.put(key, e);
        if (sortedEntries != null) {
            if (sortedEntries.size() >= settings.maxEntries()) {
                CacheEntry r = sortedEntries.poll();
                cache.remove(r.getKey());
            }
            sortedEntries.add(e);
        }
    }
    
    public void invalidateCache(MethodCall methodCall) {
        invalidateCache(kp.getKey(methodCall));
    }    
    
    private synchronized void invalidateCache(String key) {
        cache.remove(key);
    }
    
    public void invalidateCache() {
        cache.clear();
    }    
}
