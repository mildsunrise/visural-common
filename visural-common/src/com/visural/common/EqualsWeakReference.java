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
package com.visural.common;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * A weak reference which can be used in a Set<> correctly.
 * @author Visural
 * @param <T> 
 */
public class EqualsWeakReference<T> extends WeakReference<T> {

    private int hashCode;

    public EqualsWeakReference(T referent) {
        super(referent);
        hashCode = referent.hashCode();
    }

    public EqualsWeakReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        hashCode = referent.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EqualsWeakReference && ((EqualsWeakReference) obj).get() == this.get();
    }
}
