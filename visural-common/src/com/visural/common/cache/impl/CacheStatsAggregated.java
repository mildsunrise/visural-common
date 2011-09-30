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

import com.visural.common.cache.Cache;

public class CacheStatsAggregated {
    
    private CacheSettings settings;
    private int instanceCount = 1;
    private CacheStatsSnapshot combinedStats;

    public CacheStatsAggregated(Cache settings, CacheStatsSnapshot stats) {
        this.settings = new CacheSettings(settings);
        this.combinedStats = stats;
    }
    
    public CacheStatsAggregated combine(CacheStatsSnapshot snapshot) {
        instanceCount++;
        combinedStats = combinedStats.plus(snapshot);
        return this;
    }
    
    public CacheStatsAggregated combine(CacheStatsAggregated snapshot) {
        instanceCount += snapshot.getInstanceCount();
        combinedStats = combinedStats.plus(snapshot.getCombinedStats());
        return this;
    }

    public CacheStatsSnapshot getCombinedStats() {
        return combinedStats;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public CacheSettings getSettings() {
        return settings;
    }

    public void setCombinedStats(CacheStatsSnapshot combinedStats) {
        this.combinedStats = combinedStats;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }
}
