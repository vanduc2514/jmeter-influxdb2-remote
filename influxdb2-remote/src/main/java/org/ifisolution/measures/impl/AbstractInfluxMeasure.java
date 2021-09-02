package org.ifisolution.measures.impl;

import org.ifisolution.influxdb.InfluxClient;

public abstract class AbstractInfluxMeasure {

    protected String hostName;

    //Avoid NPE in Point
    protected String testName;

    //Avoid NPE in Point
    protected String runId;

    protected InfluxClient influxClient;

    protected AbstractInfluxMeasure() {
    }

    public AbstractInfluxMeasure(InfluxClient influxClient,
                                 MeasureConfigurationProvider configurationProvider) {
        this.influxClient = influxClient;
        hostName = configurationProvider.provideHostName();
        testName = configurationProvider.provideTestName();
        runId = configurationProvider.provideRunId();
    }

    public void close() {
        this.influxClient.closeClient();
    }

}
