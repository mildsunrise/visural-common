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
package com.visural.common;

import com.visural.common.apacherepack.FastDateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date utilities
 * 
 * @version $Id: DateUtil.java 96 2010-10-13 09:45:27Z tibes80@gmail.com $
 * @author Richard Nichols
 */
public class DateUtil {

    /**
     * Makes a copy of the given Date. nulls are passed through.
     * @param date
     * @return
     */
    public static Date dateCopy(Date date) {
        return (date == null ? null : new Date(date.getTime()));
    }

    /**
     * Formats a date according to the SimpleDateFormat format string provided
     * @param d the date to display
     * @param sFormat the format string according to the SimpleDateFormat class specs
     * @return the resulting formatted string
     */
    public static String formatDate(Date d, String format) {
        return d == null ? null : FastDateFormat.getInstance(format).format(d);
    }

    public static String formatDate(Date d, String format, TimeZone timeZone) {
        return d == null ? null : FastDateFormat.getInstance(format, timeZone).format(d);
    }

    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Return a date formatted to RFC1123 suitable for Http header
     * @param d
     * @return
     */
    public static String formatHttpDate(Date d) {
        return formatDate(d, PATTERN_RFC1123);
    }

    /**
     * Return a date formatted to RFC1123 suitable for Http header
     * @param d
     * @return
     */
    public static String formatHttpDate(long d) {
        return formatHttpDate(new Date(d));
    }

    /**
     * Parses a string into Date using the SimpleDateFormat format string provided.
     * @param dateStr
     * @param format
     * @return
     */
    public static Date parseDate(String dateStr, String format) {
        return parseDate(dateStr, format, null);
    }
    
    /**
     * Parses a string into Date using the SimpleDateFormat format string provided.
     * @param dateStr
     * @param format
     * @return
     */
    public static Date parseDate(String dateStr, String format, TimeZone tz) {
        if (dateStr == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (tz != null) {
            sdf.setTimeZone(tz);
        }
        Date result = sdf.parse(dateStr, new ParsePosition(0));
        return result;
    }

    /**
     * Converts a date to a SQL timestamp
     * @param d
     * @return
     */
    public static java.sql.Timestamp dateToTimestamp(java.util.Date d) {
        if (d == null) {
            return null;
        }
        java.sql.Timestamp ts;
        ts = new java.sql.Timestamp(d.getTime());
        return ts;
    }

    /**
     * Convers a SQL timestamp to a Date
     * @param ts
     * @return
     */
    public static java.util.Date timestampToDate(java.sql.Timestamp ts) {
        if (ts == null) {
            return null;
        }
        Date d = new Date(ts.getTime());
        return d;
    }
}
