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

import com.visural.common.cache.impl.CompositeKeyProvider;
import com.visural.common.cache.impl.CacheInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.visural.common.cache.impl.CacheDataImpl;

/**
 * Guice module to enable Caching functionality.
 *
 * You may provide your own {@link KeyProvider} by overriding
 * the `getKeyProvider` method.
 *
 * @version $Id: CacheModule.java 38 2010-05-24 11:39:51Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class CacheModule extends AbstractModule {

    private final CacheInterceptor interceptor = new CacheInterceptor();

    @Override
    protected void configure() {
        bind(KeyProvider.class).to(getKeyProvider()).in(Scopes.SINGLETON);
        bind(CacheData.class).to(CacheDataImpl.class);
        bindInterceptor(Matchers.subclassesOf(Cacheable.class), Matchers.annotatedWith(Cache.class), interceptor);
        requestInjection(interceptor);
    }

    protected Class<? extends KeyProvider> getKeyProvider() {
        return CompositeKeyProvider.class;
    }

    @Provides
    public CacheInterceptor getInterceptor() {
        return interceptor;
    }

}
