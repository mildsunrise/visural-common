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
import com.visural.common.Unproxy;
import com.visural.common.cache.Cache;
import com.visural.common.cache.Cacheable;
import com.visural.common.cache.KeyProvider;
import com.visural.common.cache.MethodCall;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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

    private static Logger logger = Logger.getLogger(CacheInterceptor.class.getName());

    @Inject
    KeyProvider keyProvider;
    private Map<String, CacheDataImpl> singletonCache = new HashMap<String, CacheDataImpl>();

    public Object invoke(MethodInvocation mi) throws Throwable {
        Cache annot = (Cache) mi.getMethod().getAnnotation(Cache.class);
        Cacheable cacheable = (Cacheable) mi.getThis();

        CacheDataImpl cacheData = (CacheDataImpl) cacheable.__cacheData();
        
        MethodCall call = MethodCall.fromInvocation(mi);
        CacheEntry ce = cacheData.get(call);
        if (ce != null) {
            logger.log(Level.FINE, "Cache hit: {0}", call);
            return ce.getResult();
        } else {
            try {
                Object result = mi.proceed();
                cacheData.put(call, annot, result);
                return result;
            } finally {
                // let error pass up the stack
            }
        }
    }

    /**
     * Call back to retrieve singleton method cache for `m`
     * @param m
     * @return
     */
    protected Map<String, CacheEntry> getMethodCache(Method m) {
        String key = Unproxy.clazz(m.getDeclaringClass()).getName();
        CacheDataImpl singletonData = singletonCache.get(key);
        if (singletonData != null) {
            return singletonData.getDirectMethodCache(m);
        } else {
            return null;
        }
    }

    /**
     * Call back to retrieve and/or create singleton method cache for `m`
     * @param m
     * @param annot
     * @return
     */
    protected Map<String, CacheEntry> getAndCreateMethodCache(Method m, Cache annot) {
        String key = Unproxy.clazz(m.getDeclaringClass()).getName();
        CacheDataImpl singletonData = singletonCache.get(key);
        if (singletonData == null) {
            singletonData = createSingletonCache(key);
}
        return singletonData.getAndCreateMethodCache(m, annot);
    }

    private synchronized CacheDataImpl createSingletonCache(String cacheKey) {
        if (singletonCache.get(cacheKey) == null) {
            singletonCache.put(cacheKey, new CacheDataImpl(keyProvider));
        }
        return singletonCache.get(cacheKey);
    }
}
