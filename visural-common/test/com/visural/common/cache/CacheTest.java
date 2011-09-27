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

/**
 * @version $Id: CacheTest.java 31 2010-05-21 07:15:23Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class CacheTest extends TestCase {
    
    private Injector getInjector() {
        return Guice.createInjector(new CacheModule());
    }

    private CacheService getCache() {
        Injector i = Guice.createInjector(new CacheModule());
        return i.getInstance(CacheService.class);
    }

    private CacheService[] getCaches(int n) {
        Injector i = getInjector();
        CacheService[] cs = new CacheService[n];
        for (int x = 0; x < n; x++) {
            cs[x] = i.getInstance(CacheService.class);
        }
        return cs;
    }

    public void testBasicCache() {
        CacheService cs = getCache();

        int result = cs.longServiceToCache_5("static");
        for (int n = 0; n < 5; n++) {
            int loopresult = cs.longServiceToCache_5("static");
            assertTrue(loopresult == result);
        }
    }

    public void testCacheMax() {
        CacheService cs = getCache();
        for (int n = 0; n < 5; n++) {
            int loopresult = cs.longServiceToCache_5(""+n);
            assertTrue(loopresult == n);
        }
        for (int n = 0; n < 5; n++) {
            int loopresult = cs.longServiceToCache_5(""+n);
            assertTrue(loopresult == n);
        }
        assertTrue(0 == cs.longServiceToCache_5("0"));
        assertTrue(5 == cs.longServiceToCache_5("not cached"));
    }

    public void testNonSingletonCache() {
        CacheService[] cs = getCaches(2);
        int[] results = new int[] {cs[0].randomServiceToCache(), cs[1].randomServiceToCache()};
        for (int n = 0; n < 5; n++) {
            int loopresult = cs[n%2].randomServiceToCache();
            assertTrue(loopresult == results[n%2]);
        }
    }

    /**
     * Singleton test logic hold exactly the same as non-singleton, except we
     * distribute the method calls across the instances.
     */
    public void testBasicSingletonCache() {
        CacheService[] cs = getCaches(2);
        int result = cs[0].longSingletonToCache_5("static");
        for (int n = 0; n < 5; n++) {
            int loopresult = cs[n%2].longSingletonToCache_5("static");
            assertTrue(loopresult == result);
        }
    }

    public void testBasicSingletonCache2() {
        CacheService[] cs = getCaches(2);
        for (int n = 0; n < 5; n++) {
            int loopresult = cs[0].longSingletonToCache_5(""+n);
            assertTrue(loopresult == n);
        }
        for (int n = 0; n < 5; n++) {
            int loopresult = cs[1].longSingletonToCache_5(""+n);
            assertTrue(loopresult == n);
        }
        assertTrue(cs[1].getCounter() == 0);
    }

    /**
     * Singleton test logic hold exactly the same as non-singleton, except we
     * distribute the method calls across the instances.
     */
    public void testCacheSingletonMax() {
        CacheService[] cs = getCaches(2);
        for (int n = 0; n < 5; n++) {
            int prev = cs[n%2].getCounter();
            cs[n%2].longSingletonToCache_5(""+n);
            assertTrue(cs[n%2].getCounter() == prev + 1);
        }
        for (int n = 0; n < 5; n++) {
            int prev = cs[n%2].getCounter();
            cs[n%2].longSingletonToCache_5(""+n);
            assertTrue(cs[n%2].getCounter() <= prev);
        }
        assertTrue(0 == cs[0].longSingletonToCache_5("0"));
        assertTrue(0 == cs[1].longSingletonToCache_5("0"));
        assertTrue(3 == cs[0].longSingletonToCache_5("not cached"));
        assertTrue(3 == cs[1].longSingletonToCache_5("not cached"));
    }

    public void testInvalidation() {
        CacheService cs = getCache();
        cs.longServiceToCache_5("static");
        cs.__cacheData().invalidateCache(MethodCall.get(cs.getClass(), "longServiceToCache_5", "static"));
        int prev = cs.getCounter();
        cs.longServiceToCache_5("static");
        assertTrue(prev + 1 == cs.getCounter());
    }
    
    public void testInvalidateAllValues() {
        CacheService cs = getCache();
        cs.longServiceToCache_5("static1");
        cs.longServiceToCache_5("static2");
        cs.longServiceToCache_5("static3");
        cs.longServiceToCache_5("static4");
        cs.__cacheData().invalidateCache(MethodCall.get(cs.getClass(), "longServiceToCache_5", "static").getMethod());
        int prev = cs.getCounter();
        cs.longServiceToCache_5("static1");
        cs.longServiceToCache_5("static2");
        cs.longServiceToCache_5("static3");
        cs.longServiceToCache_5("static4");
        assertTrue(prev + 4 == cs.getCounter());
    }
    
    public void testInvalidMaxEntries() {
        CacheService cs = getCache();
        try {
            cs.invalidMaxEntries();
            fail("Should not allow maxEntries <= 0");
        } catch (IllegalArgumentException e) {
        }
    }
    
    public void testTTL() throws Exception {
        CacheService cs = getCache();
        int result = cs.longServiceToCache_3_100("foo");
        assertTrue(result == cs.longServiceToCache_3_100("foo"));
        Thread.sleep(1000);
        assertTrue(result + 1 == cs.longServiceToCache_3_100("foo"));
    }
    
    public void testFIFO() {
        CacheService cs = getCache();
        double result = cs.esFIFO(0);        
        for (int n = 1; n < 6; n++) {
            cs.esFIFO(0);
            assertTrue(cs.esFIFO(n) == cs.esFIFO(n));
        }
        assertTrue(result != cs.esFIFO(0));
    }    
    
    public void testLFU() {
        CacheService cs = getCache();
        double result0 = cs.esLFU(0);        
        double result1 = cs.esLFU(1);        
        for (int n = 2; n < 6; n++) {
            cs.esLFU(0);
            assertTrue(cs.esLFU(n) == cs.esLFU(n));
        }
        assertTrue(result0 == cs.esLFU(0));
        assertTrue(result1 != cs.esLFU(1));
    }

    public void testInvalidateSingleton() {
        CacheService cs = getCache();
        cs.longSingletonToCache_5("static");
        cs.__cacheData().invalidateCache(MethodCall.get(cs.getClass(), "longSingletonToCache_5", "static"));
        int prev = cs.getCounter();
        cs.longSingletonToCache_5("static");
        assertTrue(prev + 1 == cs.getCounter());
    }
    
    public void testSoftRef() {
        CacheService cs = getCache();
        try {
            for (int n = 0; n < 10; n++) {
                cs.bigMemHard(n);
            }        
            fail("should run out of ram");
        } catch (java.lang.OutOfMemoryError e) {
            // clear cache to free ram
            cs.__cacheData().invalidateCache(MethodCall.get(cs.getClass(), "bigMemHard", (int)0).getMethod());
        }

        for (int n = 0; n < 10; n++) {
            cs.bigMemSoft(n);
        }
    }
}
