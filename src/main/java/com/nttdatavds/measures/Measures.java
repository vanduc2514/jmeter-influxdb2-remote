package com.github.vanduc2514.measures;

import com.github.vanduc2514.influxdb.InfluxClientProxy;
import com.github.vanduc2514.measures.impl.TestResultMeasureImpl;
import com.github.vanduc2514.measures.impl.TestStateMeasureImpl;

public class Measures {

    public static TestResultMeasureImpl.TestResultMeasureBuilder
    testResultMeasureBuilder(InfluxClientProxy influxClientProxy) {
        return new TestResultMeasureImpl.TestResultMeasureBuilder(influxClientProxy);
    }

    public static TestStateMeasureImpl.TestStateMeasureBuilder
    testStateMeasureBuilder(InfluxClientProxy influxClientProxy) {
        return new TestStateMeasureImpl.TestStateMeasureBuilder(influxClientProxy);
    }

}
