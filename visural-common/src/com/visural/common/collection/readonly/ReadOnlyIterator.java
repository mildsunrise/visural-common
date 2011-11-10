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

import java.util.Iterator;

/**
 * An iterator which does not support the remove() operation.
 * 
 * @version $Id: ReadOnlyIterator.java 2 2009-11-17 12:26:31Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class ReadOnlyIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;

    public ReadOnlyIterator(Iterator<T> iterator) {
        if (iterator == null) {
            throw new IllegalArgumentException("Base iterator is required.");
        }
        this.iterator = iterator;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public T next() {
        return iterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("Invalid operation - read only collection.");
    }

    @Override
    public String toString() {
        return iterator.toString();
    }
        
}
