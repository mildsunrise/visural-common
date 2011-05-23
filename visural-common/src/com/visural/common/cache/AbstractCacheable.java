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

import com.google.inject.Inject;

/**
 * Inherit from this class to enable caching. Alternatively use this as an
 * example of how to implement the {@link Cacheable} interface.
 *
 * @version $Id: AbstractCacheable.java 38 2010-05-24 11:39:51Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public abstract class AbstractCacheable implements Cacheable {

    private transient CacheData data;

    public CacheData __cacheData() {
        return data;
    }

    @Inject
    public void __cacheData(CacheData data) {
        this.data = data;
    }
}
