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

package com.visural.common.oplock;

import com.visural.common.cache.KeyIgnore;

/**
 * @version $Id: OpLockService.java 57 2010-05-31 03:51:03Z tibes80@gmail.com $
 * @author Richard Nichols
 */
@SuppressWarnings("PMD")
public class OpLockService {
    @OpLock
    public void testService(@KeyIgnore int n) {
        System.out.println("Start.."+n);
        try {
            Thread.sleep(100);
        } catch (Throwable t) {
            //
        }
        System.out.println("End...."+n);
    }
}
