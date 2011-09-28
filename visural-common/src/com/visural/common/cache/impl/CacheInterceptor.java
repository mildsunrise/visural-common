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
import com.visural.common.cache.Cacheable;
import com.visural.common.cache.KeyProvider;
import com.visural.common.cache.MethodCall;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP interceptor that implements the {@link Cache} annotation.
 *
 * @version $Id: CacheInterceptor.java 38 2010-05-24 11:39:51Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class CacheInterceptor implements MethodInterceptor {

    private static final Logger logger = Logger.getLogger(CacheInterceptor.class.getName());
    
    @Inject KeyProvider keyProvider;
    @Inject CacheDataImpl singletonCache;
    
    private Set<WeakReference<Cacheable>> instances = null;

    public CacheInterceptor() {
    }
    
    public Object invoke(MethodInvocation mi) throws Throwable {        
        singletonCache.markAsSingletonCache(); // TODO: only needed to be called once, but no suitable place to put it.
        
        Cache annot = (Cache) mi.getMethod().getAnnotation(Cache.class);
        Cacheable cacheable = (Cacheable) mi.getThis();
        if (instances != null) {
            synchronized (this) {
                instances.add(new WeakReference<Cacheable>(cacheable));
            }
        }

        CacheDataImpl cacheData = annot.singletonCache() ? 
                singletonCache : (CacheDataImpl) cacheable.__cacheData();
        
        MethodCall call = MethodCall.fromInvocation(mi);
        CacheEntry ce = cacheData.get(call);
        if (ce != null) {
            // attempt to return result. there is a minor possibility that a
            // soft reference is cleared by GC in between CacheEntry retrieval
            // and final return to caller
            try {
                logger.log(Level.FINE, "Cache hit: {0}", call);
                return ce.getResult();
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Cache error", e);
            }
        } 

        // full execute
        try {                
            long inNano = System.nanoTime();
            Object result = mi.proceed();
            long outNano = System.nanoTime();
            long out = System.currentTimeMillis();
            cacheData.put(out, outNano-inNano, call, annot, result);
            return result;
        } finally { //NOPMD
            // let error pass up the stack
        }
    }

    public synchronized void setTrackReferences(boolean trackReferences) {
        if (instances == null && trackReferences) {
            instances = new HashSet<WeakReference<Cacheable>>();
        } else if (instances != null && !trackReferences) {
            instances = null;
        }
    }        
    
    public synchronized void clearDeferencedInstances() {
        if (instances != null) {
            Iterator<WeakReference<Cacheable>> i = instances.iterator();
            while (i.hasNext()) {
                WeakReference<Cacheable> e = i.next();
                if (e.get() == null) {
                    i.remove();
                }
            }
        }        
    }
    
    /**
     * Return stats across all registered {@link Cacheable} instances which 
     * have not been garbage collected.
     * @return 
     */
    public Map<String, Map<String, CacheStats>> getStatistics() {
        Map<String, Map<String, CacheStats>> result = new HashMap<String, Map<String, CacheStats>>();
        if (instances != null) {
            for (WeakReference<Cacheable> c : instances) {
                if (c.get() != null) {
                    Map<String, CacheStats> cs = c.get().__cacheData().getStatistics();
                    String key = c.get().getClass().getName();
                    if (result.get(key) == null) {
                        result.put(key, cs);
                    } else {
                        for (Entry<String,CacheStats> e : cs.entrySet()) {
                            if (result.get(key).get(e.getKey()) != null) {
                                result.get(key).put(e.getKey(), result.get(key).get(e.getKey()).plus(e.getValue()));
                            } else {
                                result.get(key).put(e.getKey(), e.getValue());
                            }
                        }
                    }

                }
            }
            result.put("_SingletonCaches", singletonCache.getStatistics());            
        }
        return result;
    }
    
}
