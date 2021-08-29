package org.ifisolution.measures.impl;

import org.apache.commons.lang3.StringUtils;
import org.ifisolution.influxdb.InfluxClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class AbstractInfluxMeasure {

    public static final String UNKNOWN_HOST = "Unknown Host";

    protected String hostName;

    //Avoid NPE in Point
    protected String testName = StringUtils.EMPTY;

    //Avoid NPE in Point
    protected String runId = StringUtils.EMPTY;

    protected final InfluxClient influxClient;

    public AbstractInfluxMeasure(InfluxClient influxClient) {
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = UNKNOWN_HOST;
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
