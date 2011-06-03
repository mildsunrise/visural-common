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

import java.lang.reflect.Method;

/**
 * @version $Id$
 * @author Visural
 */
public class BeanUtil {
    
    // TODO: deprecate in favour java.beans

    /**
     * Attempt to find a getter method for the given bean property. Returns
     * null if the property can not be resolved.
     * @param beanType
     * @param propertyName
     * @return
     */
    public static Method findGetter(Class beanType, String propertyName) {
        String getterName = "get" + propertyName.toUpperCase().substring(0, 1) + propertyName.substring(1);
        Method result = findMethod(beanType, getterName);
        if (result == null) {
            getterName = "is" + propertyName.toUpperCase().substring(0, 1) + propertyName.substring(1);
            result = findMethod(beanType, getterName);
        }
        return result;
    }

    private static Method findMethod(Class beanType, String name, Class... params) {
        try {
            Method method = beanType.getMethod(name, params);
            return method;
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ex) {
            throw new IllegalStateException("Reflection failed due to lack of privileges.", ex);
        }
    }

    /**
     * Attempt to find a setter method for the given bean property. Returns
     * null if the property can not be resolved.
     * @param beanType
     * @param propertyName
     * @return
     */
    public static Method findSetter(Class beanType, String propertyName) {
        try {
            String setterName = "set" + propertyName.toUpperCase().substring(0, 1) + propertyName.substring(1);
            for (Method m : beanType.getMethods()) {
                if (m.getName().equals(setterName) && m.getParameterTypes().length == 1) {
                    return m;
                }
            }
            return null;
        } catch (SecurityException ex) {
            throw new IllegalStateException("Reflection failed due to lack of privileges.", ex);
        }
    }

    /**
     * Return the data type (class) of the given bean property
     * @param bean
     * @param property
     * @return
     */
    public static Class getPropertyType(Object bean, String property) {
        if (bean == null) {
            throw new IllegalArgumentException("null bean passed for introspection.");
        }
        return getPropertyType(bean.getClass(), property);
    }

    /**
     * Return the data type (class) of the given bean property
     * @param bean
     * @param property
     * @return
     */
    public static Class getPropertyType(Class type, String property) {
        Method getter = findGetter(type, property);
        if (getter != null) {
            return getter.getReturnType();
        } else {
            Method setter = findSetter(type, property);
            if (setter != null) {
                return setter.getParameterTypes()[0];
            } else {
                throw new IllegalArgumentException("Bean of type " + type.getName() + " does not have a property '" + property + "'");
            }
        }
    }
}
