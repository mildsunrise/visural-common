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
package com.visural.common.cache;

import com.visural.common.cache.impl.CacheStatsAggregated;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Public interface for cache data to be held by cached instances.
 * 
 * @version $Id: CacheData.java 38 2010-05-24 11:39:51Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public interface CacheData {

    /**
     * Return Cache statistics for each method which has active caching.
     * Note that this operation is not guaranteed to return statistics in all
     * cases or for all methods. You should check for null and handle accordingly.
     * 
     * @param estimateMemory whether the object graph should be scanned to estimate memory usage
     * @return 
     */
    Map<String, CacheStatsAggregated> getStatistics(boolean estimateMemory); 
    
    /**
     * Invalidate any cached value for the given method call.
     * @param methodCall 
     */
    void invalidateCache(MethodCall methodCall);

    /**
     * Invalidate any cached values for all possible invocations of the given method.
     * @param method 
     */
    void invalidateCache(Method method);
}
