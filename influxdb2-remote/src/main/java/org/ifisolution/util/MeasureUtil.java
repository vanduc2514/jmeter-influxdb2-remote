package org.ifisolution.util;

public class MeasureUtil {

    public static final int SCALE = 1000;

    private MeasureUtil() {
    }

    public static long getCurrentTimeMilliSecond() {
        return System.currentTimeMillis();
    }

    public static long getCurrentTimeMicroSecond() {
        return getCurrentTimeMilliSecond() * SCALE;
    }

    public static long getCurrentTimeNanoSecond() {
        return getCurrentTimeMicroSecond() * 1000;
    }

}
