package com.github.vanduc2514.measures;

public class MeasureHelper {

    public static final int SCALE = 1000;

    private MeasureHelper() {
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
