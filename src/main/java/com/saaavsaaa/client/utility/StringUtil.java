package com.saaavsaaa.client.utility;

/*
 * Created by aaa
 */
public class StringUtil {
    /**
     * Null Or Blank.
     *
     * @param string string
     * @return isNullOrBlank
     */
    public static boolean isNullOrBlank(final String string) {
        return string == null || string.trim().length() == 0;
    }
}
