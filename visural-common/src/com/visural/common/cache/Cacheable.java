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

/**
 * Cacheable objects need to provide getter/setter for instance cache. See
 * {@link AbstractCacheable} for example implementation.
 * 
 * @version $Id: Cacheable.java 46 2010-05-25 01:34:17Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public interface Cacheable {

    CacheData __cacheData();
}
