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

/**
 * Different eviction strategies for removing items from the cache when it's full.
 * @author Visural
 */
public enum EvictionStrategy {
    
    LRU,  // LEAST RECENTLY USED (default)
    FIFO, // FIRST IN FIRST OUT
    
    // Note that LFU and LFU_TIMECOST are more costly in terms of CPU and thread
    // performance due to thread locking needing to occur for each get()
    
    LFU,  // LEAST FREQUENTLY USED
    
    /**
     * Sorts the entries by (timecost+1)*#uses and evicts the least valuable item.
     * +1 is added to timecost to ensure value >= 1.
     * Note that timecost uses System.nanoTime();
     */
    LFU_TIMECOST;
}
