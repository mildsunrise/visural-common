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

import com.google.inject.Inject;
import com.visural.common.Unproxy;
import com.visural.common.cache.KeyProvider;
import com.visural.common.cache.MethodCall;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * TODO: Performance of this could be increase by separating map into method name lookup lock collections
 * 
 * @version $Id: OpLockInterceptor.java 57 2010-05-31 03:51:03Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class OpLockInterceptor implements MethodInterceptor {

    private static final Logger logger = Logger.getLogger(OpLockInterceptor.class.getName());

    @Inject KeyProvider keyProvider;

    private List<String> locks = new ArrayList<String>();
    private final long lockTimeout;

    public OpLockInterceptor(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public Object invoke(MethodInvocation mi) throws Throwable {       
        MethodCall call = MethodCall.fromInvocation(mi);
        String key = Unproxy.clazz(call.getMethod().getDeclaringClass()).getName()+"."+call.getMethod().getName()+"#"+keyProvider.getKey(call);
        boolean gotLock = false;
        try {
            long inTime = System.currentTimeMillis();
            while (!(gotLock = setLock(key))) {
                if (System.currentTimeMillis() > inTime + lockTimeout) {
                    throw new IllegalStateException("Trying method call "+call.toString()+" but still locked after timeout ("+lockTimeout+" ms).");
                }
                try {
                    Thread.sleep(5);
                } catch (Throwable t) {
                    logger.log(Level.WARNING, "Thread.sleep() failed", t);
                }
            }
            // got lock
            return mi.proceed();
        } finally {
            if (gotLock) {
                releaseLock(key);
            }
        }
    }

    private synchronized boolean setLock(String key) {
        if (locks.contains(key)) {
            return false;
        } else {
            locks.add(key);
            return true;
        }
    }

    private synchronized void releaseLock(String key) {
        locks.remove(key);
    }

}
