/*
 *  Copyright 2009 Richard Nichols.
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
package com.visural.common.datastruct;

import java.util.regex.Pattern;

/**
 * @version $Id: StringPosition.java 26 2010-03-10 06:41:38Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class StringPosition {

    private final String str;
    private final int position;
    private int lastMatchCharCount = 0;

    private boolean returnAndSetMatch(boolean stmt, int matchCount) {
        if (stmt) {
            lastMatchCharCount = matchCount;
        }
        return stmt;
    }

    public StringPosition(String str, int position) {
        this.str = str;
        this.position = position;
    }

    public int getLastMatchCharCount() {
        return lastMatchCharCount;
    }

    public int getPosition() {
        return position;
    }

    public String getString() {
        return str;
    }

    public String getStringFromPosition(int len) {
        int end = position+len;
        if (end > str.length()) {
            end = str.length();
        }
        return str.substring(position, end);
    }
    
    private int startPos(String other, boolean inclusive) {
        return position - other.length() + (inclusive ? 1 : 0);
    }

    private int endPos(String other, boolean inclusive) {
        return position + other.length() + (inclusive ? 0 : 1);
    }

    private boolean aheadRegex(String regex, boolean inclusive, boolean ignoreCase) {
        if (!Pattern.compile(regex+".*", Pattern.DOTALL|(ignoreCase ? Pattern.CASE_INSENSITIVE : 0)).matcher(str.substring((inclusive ? position : position+1))).matches()) {
            return false;
        }
        Pattern p = Pattern.compile(regex, Pattern.DOTALL|(ignoreCase ? Pattern.CASE_INSENSITIVE : 0));
        for (int n = (inclusive ? position+1 : position+2); n <= str.length(); n++) {
            String current = str.substring((inclusive ? position : position+1), n);
            if (p.matcher(current).matches()) {
                lastMatchCharCount = current.length();
                return true;
            }
        }
        return false;
    }

    public boolean aheadRegex(String regex, boolean inclusive) {
        return aheadRegex(regex, inclusive, false);
    }

    public boolean aheadRegexIgnoreCase(String regex, boolean inclusive) {
        return aheadRegex(regex, inclusive, true);
    }

    public boolean aheadInRegex(String[] options, boolean inclusive) {
        for (String s : options) {
            if (aheadRegex(s, inclusive)) {
                return true;
            }
        }
        return false;
    }

    public boolean aheadInRegexIgnoreCase(String[] options, boolean inclusive) {
        for (String s : options) {
            if (aheadRegexIgnoreCase(s, inclusive)) {
                return true;
            }
        }
        return false;
    }

    public boolean aheadIn(String[] options, boolean inclusive) {
        for (String s : options) {
            if (aheadEquals(s, inclusive)) {
                return true;
            }
        }
        return false;
    }

    public boolean aheadInIgnoreCase(String[] options, boolean inclusive) {
        for (String s : options) {
            if (aheadEqualsIgnoreCase(s, inclusive)) {
                return true;
            }
        }
        return false;
    }

    public boolean behindIn(String[] options, boolean inclusive) {
        for (String s : options) {
            if (behindEquals(s, inclusive)) {
                return true;
            }
        }
        return false;
    }

    public boolean behindInIgnoreCase(String[] options, boolean inclusive) {
        for (String s : options) {
            if (behindEqualsIgnoreCase(s, inclusive)) {
                return true;
            }
        }
        return false;
    }

    public boolean behindEquals(String other, boolean inclusive) {
        int start = startPos(other, inclusive);
        if (start < 0) {
            return false;
        } else {
            return returnAndSetMatch(str.substring(start, start + other.length()).equals(other), other.length());
        }
    }

    public boolean aheadEquals(String other, boolean inclusive) {
        int end = endPos(other, inclusive);
        if (end > str.length()) {
            return false;
        } else {
            return returnAndSetMatch(str.substring((inclusive ? position : position + 1), end).equals(other), other.length());
        }
    }

    public boolean behindEqualsIgnoreCase(String other, boolean inclusive) {
        int start = startPos(other, inclusive);
        if (start < 0) {
            return false;
        } else {
            return returnAndSetMatch(str.substring(start, start + other.length()).equalsIgnoreCase(other), other.length());
        }
    }

    public boolean aheadEqualsIgnoreCase(String other, boolean inclusive) {
        int end = endPos(other, inclusive);
        if (end > str.length()) {
            return false;
        } else {
            return returnAndSetMatch(str.substring((inclusive ? position : position + 1), end).equalsIgnoreCase(other), other.length());
        }
    }
}
