/*
    Copyright (c) 1996-2009 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/FrameworkUtil.java#1 $

    Responsible: gforget
*/

package ariba.ai.testing.util;

import ariba.util.core.Assert;
import ariba.util.core.ClassUtil;
import ariba.util.core.StringUtil;
import ariba.util.core.SystemUtil;
import ariba.util.core.Fmt;
import ariba.util.log.LogManager;
import ariba.util.log.Logger;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import ariba.ai.testing.util.MessageDigestUtil;

public class FrameworkUtil
{
    public static String AccentedStringReplacementString = "-";
    public static String AccentedStringPattern = "[\\x80-\\xFF]+";


  
    /**
        Strips the directory of the rootSuite

        @param name name of the directory to fix
        @return name of the directory without the final fileseparator if any

        @aribaapi private
    */
    private static String stripDir (String name)
    {
        String temp = name.trim();
        if (temp.charAt(temp.length() - 1) == '\\' ||
            temp.charAt(temp.length() - 1) == '/' ) {
            return temp.substring(0, temp.length() - 1);
        }
        else {
            return temp;
        }
    }


    public static String getTestShortIdFromId (String testId)
    {
        // short testid is always generated replacing # by .
        testId = formatTestId(testId);
        if (!StringUtil.nullOrEmptyString(testId)) {
            return MessageDigestUtil.getMessageDigest(testId.getBytes(), "SHA-256");
        }
        return null;
    }
    
    /**
     * AW: 26/01/2010: testId generation for selenium tests changed. The suite and script name (without .csv)
     * are now separated with # instead of '.'. This replacement provides backward compatibility
     * so that old nad new testIds matches. This method should ononly be used as
     * a fallback. 
     * @deprecated
     * @param testId
     * @return old style testId
     */
    public static String formatTestId(String testId) {
        if (!StringUtil.nullOrEmptyString(testId)) {
            testId = testId.replaceAll("#","\\.");
        }
        return testId;
    }
  
    /*
       Returns the value of system property ignore.htm.report
       If this value is set to false(ignore case),
       will print HTM reports.
    */

    public static boolean ignoreHtmReport ()
    {
        String ignoreHtmReportStr = SystemUtil.getenv("IGNORE_HTM_REPORT");
        if ( ignoreHtmReportStr == null ) {
            ignoreHtmReportStr = "false";
        }
        return ignoreHtmReportStr.equalsIgnoreCase("true");
    }

    /**
        This is a helper method to be used by unit tests run from the server
        which can turn on some logging ane make sure they have some
        appenders registered to them
        @param logger the category to enable
        @param level the severity to enable
        @return the previous level for that severity
    */
    public static Level setLevelAndDefaultAppenders (Logger logger, Level level)
    {
        Enumeration appenders = logger.getAllAppenders();
        if (! appenders.hasMoreElements()) {
            Appender appender = LogManager.getCommonConsoleAppender();
            if (appender != null) {
                logger.addAppender(appender);
            }
            appender = LogManager.getCommonFileAppender();
            if (appender != null) {
                logger.addAppender(appender);
            }
        }
        Level returnValue = logger.getLevel();
        logger.setLevel(level);
        return returnValue;
    }

    /*
        If there's accented char in the string, replace all the occurrences.
        If there's no accented char, return immediately
        If nullpointer, return empty string
    */
    public static String filterAccentedCharIfAny (String input)
    {
        if( input == null) {
            return "";
        }
        
        String result = input;
        Pattern accentedCharPattern = Pattern.compile(AccentedStringPattern);
        Matcher accentedCharMatcher = accentedCharPattern.matcher(input);
        if (accentedCharMatcher.find()) {
            result = accentedCharMatcher.replaceAll(AccentedStringReplacementString);
        }
        return result;
    }


    /**
        Helper method to get the name of the suite from its location
    */
    public static String stripSuiteFromSuiteLocation (String location)
    {
        int cutPosition;
        cutPosition = location.lastIndexOf('\\');
        if (cutPosition == -1) {
            cutPosition = location.lastIndexOf('/');
        }
        if (cutPosition == -1) {
            return location;
        }
        return location.substring(cutPosition + 1);
    }


    /**
        Helper method to get the path of the suite from its location
    */
    public static String stripPathFromSuiteLocation (String location)
    {
        String path = suitePathFromSuiteLocation(location);
        path = path.replace('\\', '.');
        path = path.replace('/', '.');
        return path;
    }

    public static String suitePathFromSuiteLocation (String location)
    {
        int cutPosition;
        cutPosition = location.lastIndexOf('\\');
        if (cutPosition == -1) {
            cutPosition = location.lastIndexOf('/');
        }
        if (cutPosition == -1) {
            return location;
        }

        return location.substring(0, cutPosition);
    }

    /**
     * Helper method serlializing an array as string using "/" as separator.
     * If the array is <code>null</code> or empty, the string <i>&lt;no values specified&gt;</i>
     * is returned.
     * @param arr the array to serialize
     * @return the string concatenation of the values in the array
     * @aribaapi private
     */
    public static String arrayToString (String[] arr)
    {
        if (arr != null && arr.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i=0; i<arr.length; i++) {
                builder.append(arr[i]);
                if (i<arr.length-1) {
                    builder.append("/");
                }
            }
            return builder.toString();
        }
        else {
            return "<no values specified>";
        }
    }

    private static final long SECOND_IN_MILLIS = 1000;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    private static final long HOUR_IN_MILLIS = 60 * 60 * 1000;
    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    /**
     * Formats duration in user friendly format ex: 1d 32m 4ms  
     * @param millis duration in milliseconds
     * @return formated time
     */
    public static String formatDuration (long millis)
    {
        long d = millis / DAY_IN_MILLIS;
        long h = (millis - d * DAY_IN_MILLIS) / HOUR_IN_MILLIS;
        long m = (millis - d * DAY_IN_MILLIS - h * HOUR_IN_MILLIS) / MINUTE_IN_MILLIS;
        long s = (millis - d * DAY_IN_MILLIS - h * HOUR_IN_MILLIS - m * MINUTE_IN_MILLIS) / SECOND_IN_MILLIS;
        long ms = millis % SECOND_IN_MILLIS;
        
        StringBuilder sb = new StringBuilder();
        appendTimePart(d, "d", sb);
        appendTimePart(h, "h", sb);
        appendTimePart(m, "m", sb);
        appendTimePart(s, "s", sb);
        appendTimePart(ms, "ms", sb);
        return sb.toString();
    }

    private static void appendTimePart (long time, String timeChar, StringBuilder sb) {
        if (time > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(time).append(timeChar);
        }
    }
}
