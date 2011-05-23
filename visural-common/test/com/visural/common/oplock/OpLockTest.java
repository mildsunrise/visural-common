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

package com.visural.common.oplock;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.visural.common.cache.CacheModule;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * @version $Id: OpLockTest.java 57 2010-05-31 03:51:03Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class OpLockTest extends TestCase {
    
    private <T extends Runnable> Runnable[] getServices(Class<T> clazz, int n) {
        Injector i = Guice.createInjector(new OpLockModule(1000), new CacheModule());
        Runnable[] result = new Runnable[n];
        for (int s = 0; s < n; s++) {
            result[s] = i.getInstance(clazz);
        }
        return result;
    }

    private int NUM_THREADS = 10;

    public void testLock() throws InterruptedException {
        Runnable[] svc = getServices(OpLockFixed.class, NUM_THREADS);
        List<Thread> ts = new ArrayList();
        for (Runnable f : svc) {
            Thread t = new Thread(f);
            ts.add(t);
            t.start();
        }
        for (Thread t : ts) {
            t.join();
        }
    }

    public void testNoLock() throws InterruptedException {
        Runnable[] svc = getServices(OpLockInc.class, NUM_THREADS);
        List<Thread> ts = new ArrayList();
        for (Runnable f : svc) {
            Thread t = new Thread(f);
            ts.add(t);
            t.start();
        }
        for (Thread t : ts) {
            t.join();
        }
    }

    public static class OpLockFixed extends OpLockService implements Runnable {
        public void run() {
            testService(0);
        }
    }
    public static class OpLockInc extends OpLockService implements Runnable {
        static int n = 0;
        public void run() {
            testService(n++);
        }
    }
}
