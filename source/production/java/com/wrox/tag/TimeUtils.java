package com.wrox.tag;

/**
 * Created by EKAC on 15.10.2017 г..
 */
public final class TimeUtils
{
    public static String intervalToString(long timeInterval)
    {
        if(timeInterval < 1_000)
            return "less than one second";
        if(timeInterval < 60_000)
            return (timeInterval / 1_000) + " seconds";
        return "about " + (timeInterval / 60_000) + " minutes";
    }
}