package org.ifisolution.measures.impl;

import org.ifisolution.influxdb.InfluxClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class AbstractInfluxMeasure {

    protected String hostName;

    protected String testName;

    protected String runId;

    protected final InfluxClient influxClient;

    public AbstractInfluxMeasure(InfluxClient influxClient) {
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "Unknown Host";
        }
        this.influxClient = influxClient;
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
