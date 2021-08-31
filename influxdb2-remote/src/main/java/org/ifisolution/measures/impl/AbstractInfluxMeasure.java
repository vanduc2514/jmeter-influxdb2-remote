package org.ifisolution.measures.impl;

import org.ifisolution.influxdb.InfluxClient;

public abstract class AbstractInfluxMeasure {

    protected String hostName;

    //Avoid NPE in Point
    protected String testName;

    //Avoid NPE in Point
    protected String runId;

    protected final InfluxClient influxClient;

    public AbstractInfluxMeasure(InfluxClient influxClient,
                                 MeasureConfigurationProvider configurationProvider) {
        this.influxClient = influxClient;
        hostName = configurationProvider.provideHostName();
        testName = configurationProvider.provideTestName();
        runId = configurationProvider.provideRunId();
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getRunId() {
        return runId;
    }

    public void close() {
        this.influxClient.closeClient();
    }

}
