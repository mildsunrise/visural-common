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

import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.spi.BindingScopingVisitor;

public class GuiceUtil {

    public static String getScopeDesc(Injector i, Class clazz) {
        final Holder h = new Holder();
        i.getBinding(clazz).acceptScopingVisitor(new BindingScopingVisitor() {

            public Object visitEagerSingleton() {
                h.desc = "Singleton";
                return null;
            }

            public Object visitScope(Scope scope) {
                h.desc = ""+scope;
                return null;
            }

            public Object visitScopeAnnotation(Class type) {
                h.desc = ""+type;
                return null;
            }

            public Object visitNoScoping() {
                h.desc = "Unscoped";
                return null;
            }
        });
        return h.desc;
    }
    
    private static class Holder {
        public String desc = "unknown";
    }
}
