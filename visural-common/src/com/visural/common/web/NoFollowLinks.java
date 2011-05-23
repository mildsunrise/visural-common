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
package com.visural.common.web;

import java.util.regex.Pattern;

/**
 * @version $Id: NoFollowLinks.java 29 2010-04-09 03:35:38Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class NoFollowLinks {

    public static String REGEX = "<a\\s[^>]*href\\s*=";
    public static String REPLACE = "<a rel=\"nofollow\" href=";
    private static Pattern rep = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    public static String replace(String html) {
        return rep.matcher(html).replaceAll(REPLACE);
    }
}
