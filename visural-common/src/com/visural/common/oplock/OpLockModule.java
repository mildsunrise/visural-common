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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;

/**
 * Based on code from visural-common Guice AOP Cache (Apache 2.0 Licence)
 *
 * @version $Id: OpLockModule.java 57 2010-05-31 03:51:03Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class OpLockModule extends AbstractModule {

    private final OpLockInterceptor interceptor;

    public OpLockModule(long lockTimeout) {
        interceptor = new OpLockInterceptor(lockTimeout);
    }

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(OpLock.class), interceptor);
        requestInjection(interceptor);
    }

    @Provides
    public OpLockInterceptor getInterceptor() {
        return interceptor;
    }

}
