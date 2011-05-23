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
package com.visural.common.cache;

import com.visural.common.StringUtil;
import com.visural.common.Unproxy;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Wraps a method with an argument list (stateless).
 * 
 * @version $Id$
 * @author Richard Nichols
 */
public class MethodCall {

    private final Method method;
    private final Object[] arguments;

    public MethodCall(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MethodCall other = (MethodCall) obj;
        if (this.method != other.method && (this.method == null || !this.method.equals(other.method))) {
            return false;
        }
        if (!Arrays.deepEquals(this.arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.method != null ? this.method.hashCode() : 0);
        hash = 71 * hash + Arrays.deepHashCode(this.arguments);
        return hash;
    }

    /**
     * Generate a MethodCall from an AOP method invocation.
     * @param mi
     * @return
     */
    public static MethodCall fromInvocation(MethodInvocation mi) {
        return new MethodCall(mi.getMethod(), mi.getArguments());
    }

    /**
     * Generate a method call from a class, name and argument list, by inferring
     * the class types from the arguments. Will fail if any arguments can be null, 
     * or do not match the types declared against the Method.
     *
     * Consider this a helper method for the simple cases only.
     *
     * @param declaringClass
     * @param methodName
     * @param arguments
     * @return
     */
    public static MethodCall get(Class declaringClass, String methodName, Object... arguments) {
        Class[] types = new Class[arguments.length];
        for (int n = 0; n < arguments.length; n++) {
            if (arguments[n] == null) {
                throw new IllegalStateException("Null arguments may not be used with MethodCall.get(...). ("+
                        declaringClass.getName()+"."+methodName+"["+StringUtil.delimitObjectsToString(",", arguments)+"]"+")");
            }
            types[n] = arguments[n].getClass();
        }
        return getExplicit(declaringClass, methodName, types, arguments);
    }

    /**
     * Explict method lookup, giving argument types and argument values.
     * @param declaringClass
     * @param methodName
     * @param types
     * @param arguments
     * @return
     */
    public static MethodCall getExplicit(Class declaringClass, String methodName, Class[] types, Object[] arguments) {
        try {
            return new MethodCall(Unproxy.clazz(declaringClass).getMethod(methodName, types), arguments);
        } catch (Exception ex) {
            throw new IllegalStateException("MethodCall.get(...) did not resolve method reference ("+
                        declaringClass.getName()+"."+methodName+"["+StringUtil.delimitObjectsToString(",", arguments)+"]"+")", ex);
        }
    }

    @Override
    public String toString() {
        return method.getDeclaringClass().getName()+"."+method.getName()+"("+StringUtil.delimitObjectsToString(",", arguments)+")";
    }
}
