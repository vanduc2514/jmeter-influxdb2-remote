package com.nttdatavds.measures;

import com.nttdatavds.influxdb.InfluxClient;
import com.nttdatavds.measures.impl.TestResultMeasureImpl;
import com.nttdatavds.measures.impl.TestStateMeasureImpl;

public class Measures {

    public static TestResultMeasureImpl.TestResultMeasureBuilder
    testResultMeasureBuilder(InfluxClient influxClient) {
        return new TestResultMeasureImpl.TestResultMeasureBuilder(influxClient);
    }

    public static TestStateMeasureImpl.TestStateMeasureBuilder
    testStateMeasureBuilder(InfluxClient influxClient) {
        return new TestStateMeasureImpl.TestStateMeasureBuilder(influxClient);
    }

}
