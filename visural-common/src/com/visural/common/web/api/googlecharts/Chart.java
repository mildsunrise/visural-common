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
 * @version $Id: Chart.java 59 2010-06-07 10:25:10Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class Chart {

    public static final String apiURL = "http://chart.apis.google.com/chart";

    private static final String encodingChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // TODO: currently only works for +ve numbers
    public static String encodeSimple(Double[] values) {
        return encodeSimple(values, null);
    }
    public static String encodeSimple(Double[] values, Double forcedMax) {
        StringBuilder result = new StringBuilder();
        /*
         * A—Z, where A = 0, B = 1, and so on, to Z = 25
a—z, where a = 26, b = 27, and so on, to z = 51
0(zero)—9, where 0 = 52 and 9 = 61
The underscore character (_) indicates a missing value
         */

         Double max = null;
         for (Double cur : values) {
             if (max == null || (cur != null && cur > max)) {
                 max = cur;
             }
         }
         if (forcedMax != null) {
             max = forcedMax;
         }
         for (Double cur : values) {
             if (cur == null) {
                 result.append("_");
             } else {
                 int val = (int)(cur / max * 61);
                 result.append(encodingChars.charAt(val));
             }
         }
         return result.toString();
    }

}
