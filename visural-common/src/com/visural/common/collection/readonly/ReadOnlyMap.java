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
package com.visural.common.collection.readonly;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A map which wraps another map, but throws a
 * `UnsupportedOperationException` if the consumer attempts to modify it.
 *
 * @version $Id: ReadOnlyMap.java 2 2009-11-17 12:26:31Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class ReadOnlyMap<U, V> implements Map<U, V> {

    private final Map<U, V> baseMap;

    public ReadOnlyMap(Map<U, V> baseMap) {
        if (baseMap == null) {
            throw new IllegalArgumentException("Base map is required.");
        }
        this.baseMap = baseMap;
    }

    public int size() {
        return baseMap.size();
    }

    public boolean isEmpty() {
        return baseMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        return baseMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return baseMap.containsValue(value);
    }

    public V get(Object key) {
        return baseMap.get(key);
    }

    public V put(U key, V value) {
        throw new UnsupportedOperationException("Invalid operation - read only map.");
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException("Invalid operation - read only map.");
    }

    public void putAll(Map<? extends U, ? extends V> m) {
        throw new UnsupportedOperationException("Invalid operation - read only map.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Invalid operation - read only map.");
    }

    public Set<U> keySet() {
        return new ReadOnlySet(baseMap.keySet());
    }

    public ReadOnlyCollection<V> values() {
        return new ReadOnlyCollection(baseMap.values());
    }

    public Set<Entry<U, V>> entrySet() {
        // TODO: still possible to modify via - Entry.* but not a major issue
        return new ReadOnlySet(baseMap.entrySet());
    }
    
    @Override
    public String toString() {
        return baseMap.toString();
    }    
    
}
