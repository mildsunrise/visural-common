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
import com.visural.common.datastruct.LRUCache;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local cache implementation.
 *
 * @version $Id: CacheDataImpl.java 38 2010-05-24 11:39:51Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class CacheDataImpl implements CacheData {
    
    private Map<String,Map<String,CacheEntry>> caches = new ConcurrentHashMap<String, Map<String,CacheEntry>>();
    private final KeyProvider keyProvider;
    private final CacheInterceptor cacheInterceptor;
    private final boolean isSingletonCache;

    @Inject
    public CacheDataImpl(CacheInterceptor cacheInterceptor, KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
        this.cacheInterceptor = cacheInterceptor;
        isSingletonCache = false;
    }

    public CacheDataImpl(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
        this.cacheInterceptor = null;
        isSingletonCache = true;
    }

    public CacheEntry get(MethodCall methodCall) {
        Map<String,CacheEntry> cache = getMethodCache(methodCall.getMethod());
        if (cache != null) {
            String key = keyProvider.getKey(methodCall);
            CacheEntry ce = cache.get(key);
            if (ce != null && ce.isExpired()) {
                cache.remove(key);
                ce = null;
    }
            return ce;
        } else {
            return null;
        }
    }

    public void put(MethodCall methodCall, Cache annot, Object result) {
        Map<String,CacheEntry> cache = getAndCreateMethodCache(methodCall.getMethod(), annot);
        cache.put(keyProvider.getKey(methodCall), new CacheEntry(annot.timeToLive(), result));
    }

    protected Map<String,CacheEntry> getAndCreateMethodCache(Method m, Cache annot) {
        Map<String,CacheEntry> result = caches.get(m.toString());
        if (annot.singletonCache() && !isSingletonCache) {
        if (result == null) {
                result = cacheInterceptor.getAndCreateMethodCache(m, annot);
                caches.put(m.toString(), result);
            }
            } else {
            int maxEntries = annot.maxEntries();
            if (result == null) {
                if (maxEntries > 0) {
                    result = Collections.synchronizedMap(new LRUCache(maxEntries));
                } else {
                result = Collections.synchronizedMap(new LRUCache());
            }
            caches.put(m.toString(), result);
        }
        }
        return result;
    }
    
    protected Map<String,CacheEntry> getMethodCache(Method m) {
        Map<String,CacheEntry> result = getDirectMethodCache(m);
        if (result == null) {
            result = cacheInterceptor.getMethodCache(m);
            if (result != null) {
                caches.put(m.toString(), result);
}
        }
        return result;
    }

    protected Map<String,CacheEntry> getDirectMethodCache(Method m) {
        return caches.get(m.toString());
    }

    public void invalidateCache(MethodCall methodCall) {
        Map<String,CacheEntry> cache = getMethodCache(methodCall.getMethod());
        if (cache != null) {
            cache.remove(keyProvider.getKey(methodCall));
        }
    }
    
}
