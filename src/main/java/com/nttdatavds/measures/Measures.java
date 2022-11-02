package com.nttdatavds.measures;

import com.nttdatavds.influxdb.InfluxClientProxy;
import com.nttdatavds.measures.impl.TestResultMeasureImpl;
import com.nttdatavds.measures.impl.TestStateMeasureImpl;

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
