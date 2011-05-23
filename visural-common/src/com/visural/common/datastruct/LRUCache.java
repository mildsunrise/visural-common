/*
 *  Copyright 2009 Richard Nichols.
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
package com.visural.common.datastruct;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basic Least Recently Used cache.
 *
 * @author Richard Nichols
 * @param <K>
 * @param <V>
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {

    /**
     * Default maximum size of cache 
     */
    public static final int DEFAULT_MAX_SIZE = 1000;

    private static final float HASHTABLELOAD = 0.75f;
    
    private int maxSize = DEFAULT_MAX_SIZE;

    /**
     * Creates a new LRUCache with the default size
     */
    public LRUCache() {
        super((int)Math.ceil(DEFAULT_MAX_SIZE / HASHTABLELOAD)+1, HASHTABLELOAD, true);
        this.maxSize = DEFAULT_MAX_SIZE;
    }

    /**
     * Creats a new LRUCache with the given maximum size
     * @param maxSize
     */
    public LRUCache(int maxSize) {
        super((int)Math.ceil(maxSize / HASHTABLELOAD)+1, HASHTABLELOAD, true);
        this.maxSize = maxSize;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return this.size() > maxSize;
    }    
}
