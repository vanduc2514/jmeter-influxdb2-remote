package org.ifisolution.util;

import com.influxdb.client.write.Point;

import java.util.concurrent.TimeUnit;

public class MeasureUtil {

    public static final int SCALE = 1000;

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
