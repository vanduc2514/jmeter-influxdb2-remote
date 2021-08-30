package org.ifisolution.measures.impl;

import org.apache.commons.lang3.StringUtils;
import org.ifisolution.influxdb.InfluxClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractInfluxMeasure {

    public static final String UNKNOWN_HOST = "Unknown Host";

    public static final String DATE_PATTERN = "yyyy-MM-dd_HH:mm:ss";
    public static final String DEFAULT_RUN_ID = "R001";
    public static final String JMETER_TEST_PLAN_DEFAULT = "Jmeter_TestPlan_Default";

    protected String hostName;

    //Avoid NPE in Point
    protected String testName = JMETER_TEST_PLAN_DEFAULT;

    //Avoid NPE in Point
    protected String runId = DEFAULT_RUN_ID;

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

    private String getMeasureDateAsString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return LocalDateTime.now().format(formatter);
    }

}
