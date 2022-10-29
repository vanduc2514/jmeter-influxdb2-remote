package com.nttdatavds.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;

public class PluginConfiguration {

    public static final String DEFAULT_TEST_NAME = "Jmeter_TestPlan_Default";
    public static final String DEFAULT_RUN_ID = "R001";
    public static final String EMPTY = StringUtils.EMPTY;

    public String influxConnectionUrl() {
        String url = StringUtils.EMPTY;
        boolean enableSSL = JMeterUtils.getPropDefault("influxdb.ssl.enable", false);
        String hostName = JMeterUtils.getPropDefault("influxdb.hostname", EMPTY);
        String port = JMeterUtils.getPropDefault("influxdb.port", EMPTY);
        return url + (enableSSL ? "https" : "http") + "://" + hostName + ":" + port;
    }

    public String influxToken() {
        return JMeterUtils.getPropDefault("influxdb.token", EMPTY);
    }

    public String influxOrganizationName() {
        return JMeterUtils.getPropDefault("influxdb.organization", EMPTY);
    }

    public String influxBucketName() {
        return JMeterUtils.getPropDefault("influxdb.bucket", EMPTY);
    }

    public String testName() {
        return JMeterUtils.getPropDefault("test.name", DEFAULT_TEST_NAME);
    }

    public String testRunId() {
        return JMeterUtils.getPropDefault("test.runId", DEFAULT_RUN_ID);
    }

    public boolean saveErrorResponse() {
        return JMeterUtils.getPropDefault("measure.save.error", false);
    }

    public boolean measureSubResult() {
        return JMeterUtils.getPropDefault("measure.sub.result", false);
    }

    public int userMetricInterval() {
        return JMeterUtils.getPropDefault("measure.user.interval", 1);
    }

    public int userMetricPoolSize() {
        return JMeterUtils.getPropDefault("measure.user.pool", 10);
    }

    public int writeBatchSize() {
        return JMeterUtils.getPropDefault("write.batch", 1000);
    }

    public int writeFlushInterval() {
        return JMeterUtils.getPropDefault("write.flush.interval", 1000);
    }

    public int writeBufferLimit() {
        return JMeterUtils.getPropDefault("write.buffer.limit", 10000);
    }
}
