package org.ifisolution.measures.impl;

import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;

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


    /**
     * Configure the {@link InfluxTestResultMeasureImpl} with the Jmeter properties
     */
    public void configureMeasure() {
        InfluxPropertiesProvider propertiesProvider = new InfluxPropertiesProvider();
        influxClient = InfluxClient.buildClient(new InfluxClientConfiguration(propertiesProvider));
        testName = propertiesProvider.provideTestName();
        runId = propertiesProvider.provideRunId();
        hostName = propertiesProvider.provideHostName();
    }

}
