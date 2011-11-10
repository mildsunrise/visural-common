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

import java.util.Collection;

/**
 * A collection which wraps another collection, but throws a
 * `UnsupportedOperationException` if the consumer attempts to modify it.
 *
 * @version $Id: ReadOnlyCollection.java 2 2009-11-17 12:26:31Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class ReadOnlyCollection<T> implements Collection<T> {
    
    private final Collection<T> baseCollection;

    public ReadOnlyCollection(Collection<T> baseCollection) {
        if (baseCollection == null) {
            throw new IllegalArgumentException("Base collection is required.");
        }
        this.baseCollection = baseCollection;
    }

    public int size() {
        return baseCollection.size();
    }

    public boolean isEmpty() {
        return baseCollection.isEmpty();
    }

    public boolean contains(Object o) {
        return baseCollection.contains(o);
    }

    public ReadOnlyIterator<T> iterator() {
        return new ReadOnlyIterator<T>(baseCollection.iterator());
    }

    public Object[] toArray() {
        return baseCollection.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return baseCollection.toArray(a);
    }

    public boolean add(T e) {
        throw new UnsupportedOperationException("Invalid operation - read only collection.");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Invalid operation - read only collection.");
    }

    public boolean containsAll(Collection<?> c) {
        return baseCollection.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("Invalid operation - read only collection.");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Invalid operation - read only collection.");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Invalid operation - read only collection.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Invalid operation - read only collection.");
    }
    
    @Override
    public String toString() {
        return baseCollection.toString();
    }    
    
}
