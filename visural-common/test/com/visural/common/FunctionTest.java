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

import junit.framework.TestCase;

/**
 *
 * @author Visural
 */
public class FunctionTest extends TestCase {
    
    public void testArrayify() {
        String[] result = Function.arrayify(String.class, "test", 5);
        assertTrue(result.length == 5);
        for (String r : result) {
            assertTrue(r.equals("test"));
        }
    }
    
    public void testMinComparable() {
        assertTrue(Float.valueOf(5).equals(Function.min(Float.valueOf(8), Float.valueOf(5), Float.valueOf(6))));
    }
    
    public void testMaxComparable() {
        assertTrue(Float.valueOf(8).equals(Function.max(Float.valueOf(8), Float.valueOf(5), Float.valueOf(6))));        
    }
}
