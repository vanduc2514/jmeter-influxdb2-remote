package org.ifisolution.measures.impl;

import org.ifisolution.configuration.MeasureSettings;
import org.ifisolution.influxdb.InfluxClient;
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

    public void closeInfluxConnection() {
        influxClient.closeClient();
        LOGGER.info("Connection to Influx @ {} closed", influxClient.getUrl());
    }

}
