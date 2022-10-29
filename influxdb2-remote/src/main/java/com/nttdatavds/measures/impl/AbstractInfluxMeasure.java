package com.nttdatavds.measures.impl;

import com.nttdatavds.configuration.MeasureSettings;
import com.nttdatavds.influxdb.InfluxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInfluxMeasure {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractInfluxMeasure.class);

    protected String hostName;

    //Avoid NPE in Influx Point
    protected final String testName;

    //Avoid NPE in Influx Point
    protected final String runId;

    protected final InfluxClient influxClient;

    public AbstractInfluxMeasure(InfluxClient influxClient,
                                 MeasureSettings measureSettings) {
        this.influxClient = influxClient;
        hostName = measureSettings.getHostName();
        testName = measureSettings.getTestName();
        runId = measureSettings.getTestRunId();
    }

    public AbstractInfluxMeasure(String hostName, String testName, String runId, InfluxClient influxClient) {
        this.hostName = hostName;
        this.testName = testName;
        this.runId = runId;
        this.influxClient = influxClient;
    }

    public void closeInfluxConnection() {
        influxClient.closeClient();
        LOGGER.info("Connection to Influx @ {} closed", influxClient.getUrl());
    }

}
