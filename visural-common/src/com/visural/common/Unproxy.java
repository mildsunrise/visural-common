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

/**
 * Unproxies a proxied class from Guice.
 * 
 * @version $Id$
 * @author Richard Nichols
 */
public class Unproxy {

    public static Class clazz(Class clazz) {
        if (clazz == null) {
            return null;
        }
        if (clazz.getName().contains("$$EnhancerByGuice")) {
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }
}
