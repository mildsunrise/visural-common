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
package com.visural.common.cache.impl;

import com.visural.common.cache.KeyIgnore;
import com.visural.common.cache.KeyProvider;
import com.visural.common.cache.MethodCall;
import com.visural.common.cache.WithCacheId;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Key provider that uses argument.toString() and detected {@link WithCacheId}
 * @author Richard Nichols
 */
public class StandardKeyProvider implements KeyProvider {
    
    public String getKey(MethodCall methodCall) {
        StringBuilder key = new StringBuilder(/*methodCall.getMethod().getName()*/);
        int n = 0;
        for (Object o : methodCall.getArguments()) {
            if (!hasIgnoreAnnotation(methodCall.getMethod(), n++)) {
                key.append("~~");
                if (o != null && WithCacheId.class.isAssignableFrom(o.getClass())) {
                    WithCacheId wci = WithCacheId.class.cast(o);
                    key.append(wci.__cacheId());
                } else if (o != null) {
                    key.append(o.toString());
                }
            }
        }
        return key.toString();
    }

    private boolean hasIgnoreAnnotation(Method method, int i) {
        Annotation[] as = method.getParameterAnnotations()[i];
        for (Annotation a : as) {
            if (KeyIgnore.class.isAssignableFrom(a.annotationType())) {
                return true;
            }
        }
        return false;
    }
}
