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

import com.google.inject.Inject;
import com.visural.common.cache.Cache;
import com.visural.common.cache.CacheData;
import com.visural.common.cache.KeyProvider;
import com.visural.common.cache.MethodCall;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Local cache implementation.
 *
 * @version $Id: CacheDataImpl.java 38 2010-05-24 11:39:51Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class CacheDataImpl implements CacheData {

    private Map<String, MethodCache> caches = new HashMap<String, MethodCache>();
    private final KeyProvider keyProvider;
    private final CacheInterceptor interceptor;
    private boolean singletonCache = false;

    @Inject
    public CacheDataImpl(KeyProvider keyProvider, CacheInterceptor interceptor) {
        this.keyProvider = keyProvider;
        this.interceptor = interceptor;
    }
    
    public boolean isEmpty() {
        return caches.isEmpty();
    }

    public CacheEntry get(MethodCall methodCall) {
        MethodCache cache = getMethodCache(methodCall.getMethod());
        if (cache != null) {
            return cache.get(methodCall);
        } else {
            return null;
        }
    }
    
    public void markAsSingletonCache() {
        singletonCache = true;
    }

    public void put(long created, long timeCost, MethodCall methodCall, Cache annot, Object result) {
        MethodCache cache = getAndCreateMethodCache(methodCall.getMethod(), annot);
        cache.put(created, timeCost, methodCall, result);
    }

    protected MethodCache getAndCreateMethodCache(Method m, Cache annot) {
        MethodCache result = getMethodCache(m);
        if (result == null) {
            result = createMethodCache(m, annot);
        }
        return result;
    }
    
    private synchronized MethodCache createMethodCache(Method m, Cache annot) {
        MethodCache result = getMethodCache(m);
        if (result == null) {
            result = new MethodCache(annot, m, keyProvider);
            caches.put(getMethodString(m), result);
        }
        return result;
    }
    
    protected MethodCache getMethodCache(Method m) {
        return caches.get(getMethodString(m));
    }

    // Method.toString() is actually quite expensive so we use a local cache for this
    private Map<Method, String> methodToStringCache = Collections.synchronizedMap(new HashMap<Method, String>());
    
    private String getMethodString(Method m) {
        String key = methodToStringCache.get(m);
        if (key == null) {
            key = m.toString();
            methodToStringCache.put(m, key);
        }
        return key;        
    }
        
    public void invalidateCache(MethodCall methodCall) {
        if (methodCall.getMethod().getAnnotation(Cache.class).singletonCache() && !singletonCache) {
            interceptor.singletonCache.invalidateCache(methodCall);
        } else {
            MethodCache cache = getMethodCache(methodCall.getMethod());
            if (cache != null) {
                cache.invalidateCache(methodCall);
            }            
        }
    }

    public void invalidateCache(Method method) {
        if (method.getAnnotation(Cache.class).singletonCache() && !singletonCache) {
            interceptor.singletonCache.invalidateCache(method);
        } else {
            MethodCache cache = getMethodCache(method);
            if (cache != null) {
                cache.invalidateCache();
            }
        }
    }
    
    public Map<String, CacheStatsAggregated> getStatistics(boolean estimateMemory) {
        Map<String, CacheStatsAggregated> result = new HashMap<String, CacheStatsAggregated>();
        Set<String> keys;
        synchronized (this) {
            keys = new HashSet<String>(caches.keySet());
        }
        for (String key : keys) {
            result.put(key, new CacheStatsAggregated(caches.get(key).getSettings(), caches.get(key).getStatsSnapshot(estimateMemory)));
        }
        return result;
    }
}
