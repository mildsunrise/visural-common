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
 * @version $Id: WeatherIcon.java 31 2010-05-21 07:15:23Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public enum WeatherIcon {
    clear_night_moon,
    cloudy_heavy,
    cloudy_sunny,
    cloudy,
    rain,
    rainy_sunny,
    snow,
    snowflake,
    snowy_sunny,
    sunny_cloudy,
    sunny,
    thermometer_cold,
    thermometer_hot,
    thunder,
    windy;

    public String getIconName() {
        return name().replace('_', '-');
    }
}
