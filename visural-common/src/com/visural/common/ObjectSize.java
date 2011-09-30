/*
 *  Copyright 2010 Visural.
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Uses reflection to get a rough guesstimate of how much memory an Object
 * graph is using. Will not be very accurate, but is useful to get a rough
 * idea of runtime memory use for any type of object graph, without the use
 * of instrumentation.
 * @author Richard Nichols
 */
public class ObjectSize {

    public static final Map<Class, Integer> primitiveSizes;
    public static final int refSize = 4;
    public static final int classSize = 8;
    public static final int arraySize = 16;

    static {
        primitiveSizes = new HashMap<Class, Integer>();
        primitiveSizes.put(boolean.class, Integer.valueOf(1));
        primitiveSizes.put(byte.class, Integer.valueOf(1));
        primitiveSizes.put(char.class, Integer.valueOf(2));
        primitiveSizes.put(short.class, Integer.valueOf(2));
        primitiveSizes.put(int.class, Integer.valueOf(4));
        primitiveSizes.put(long.class, Integer.valueOf(8));
        primitiveSizes.put(float.class, Integer.valueOf(4));
        primitiveSizes.put(double.class, Integer.valueOf(8));
    }

    /**
     * Uses reflection to get a rough guesstimate of how much memory an Object
     * graph is using. Will not be very accurate, but is useful to get a rough
     * idea of runtime memory use for any type of object graph, without the use
     * of instrumentation.
     * @param obj
     * @return 
     */
    public static int estimate(Object obj) {
        Set visited = new HashSet();
        Map<Class, Field[]> classFieldMap = new HashMap();
        return estimate(obj, visited, classFieldMap);
    }

    private static int estimate(Object o, Set visited, Map<Class, Field[]> classFieldMap) {        
        Class c = o.getClass();
        if (c.isPrimitive()) {
            return primitiveSizes.get(c);
        }
        // see if already visited or special case for string
        if (visited.contains(o) || internalPooled(o)) {
            return 0;
        }
        visited.add(o);

        int total = 0;
        if (c.isArray()) {
            total += arraySize;
            int len = Array.getLength(o);
            if (c.getComponentType().isPrimitive()) {
                // shortcut for primitize arrays
                total += len * primitiveSizes.get(c.getComponentType());
            } else {
                for (int n = 0; n < len; n++) {
                    Object av = Array.get(o, n);
                    if (av != null) {
                        total += refSize + estimate(av, visited, classFieldMap);
                    }                    
                }
            }
        } else {
            total += classSize;
            while (c != null) {
                // performance is constrained by Field duplication
                boolean mark = false;
                Field[] fields = classFieldMap.get(c);
                if (fields == null) {
                    fields = c.getDeclaredFields();
                    for (Field f : fields) f.setAccessible(true);
                    classFieldMap.put(c, fields);
                }
                for (Field f : fields) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        if (f.getType().isPrimitive()) {
                            total += primitiveSizes.get(f.getType());
                        } else {
                            try {                                    
                                Object fv = f.get(o);
                                if (fv != null) {
                                    total += refSize + estimate(fv, visited, classFieldMap);
                                }
                            } catch (IllegalAccessException ex) {
                                throw new IllegalStateException("Unable to read field '"+
                                        f.getName()+"' on class '"+c.getName()+"'. "
                                        + "Unable to estimate object sizes.", ex);
                            }
                        }
                    }
                }
                c = c.getSuperclass();
            }
        }
        // round up to nearest 8 bytes
        int remainder = total % 8;
        if (remainder > 0) {
            total += 8 - remainder;
        }
        return total;
    }

    private static boolean internalPooled(Object o) {
        return (o instanceof String && ((String) o).intern() == o) 
                || (o instanceof Boolean && Boolean.valueOf(((Boolean) o).booleanValue()) == o)
                || (o instanceof Byte && Byte.valueOf(((Byte) o).byteValue()) == o)
                || (o instanceof Character && Character.valueOf(((Character) o).charValue()) == o)
                || (o instanceof Short && Short.valueOf(((Short) o).shortValue()) == o)
                || (o instanceof Integer && Integer.valueOf(((Integer) o).intValue()) == o)
                || (o instanceof Long && Long.valueOf(((Long) o).longValue()) == o)
                || (o instanceof Float && Float.valueOf(((Float) o).floatValue()) == o)
                || (o instanceof Double && Double.valueOf(((Double) o).doubleValue()) == o)
                ;            
    }    
}