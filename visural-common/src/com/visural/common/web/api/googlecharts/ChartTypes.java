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
package com.visural.common.web.api.googlecharts;

/**
 * @version $Id: ChartTypes.java 31 2010-05-21 07:15:23Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public enum ChartTypes {

    google_o_meter("gom"),
    line_chart("lc"),
    spark_lines("ls"),
    line_x_y("lxy"),
    pie("p"),
    pie3d("p3"),
    concentric_pie("pc"),
    horizontal_bar_stacked("bhs"),
    vertical_bar_stacked("bvs"),
    horizontal_bar_grouped("bhg"),
    vertical_bar_grouped("bvg");

    ChartTypes(String id) {
        this.id = id;
    }
    private final String id;

    public String getId() {
        return id;
    }
}
