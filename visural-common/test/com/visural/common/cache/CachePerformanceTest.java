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

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.TestCase;

public class CachePerformanceTest extends TestCase {

    private CacheService getCache() {
        Injector i = Guice.createInjector(new CacheModule());
        return i.getInstance(CacheService.class);
    }
    
    public void testPerf() {
        CacheService cs = getCache();
        
        int count = 0;
        long start = System.currentTimeMillis();
        
        while (System.currentTimeMillis() < start + 1000*20) {
            cs.simpleMethod(Long.toString(System.currentTimeMillis()));
            count++;
        }        
        System.out.println("Called "+count+" times");
    }
}
