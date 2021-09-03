package org.ifisolution.measures.impl;

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
                                 MeasureConfigurationProvider configurationProvider) {
        this.influxClient = influxClient;
        hostName = configurationProvider.provideHostName();
        testName = configurationProvider.provideTestName();
        runId = configurationProvider.provideRunId();
    }

    public void closeInfluxConnection() {
        influxClient.closeClient();
        LOGGER.info("Connection to Influx @ {} closed", influxClient.getHostName());
    }

}
