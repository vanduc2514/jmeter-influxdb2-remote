package org.ifisolution.configuration;

import org.apache.jmeter.util.JMeterUtils;

import static org.ifisolution.configuration.JmeterProperties.*;

public class ClientProperties {

    public static final String DEFAULT_TEST_NAME = "Jmeter_TestPlan_Default";
    public static final String DEFAULT_RUN_ID = "R001";

    public String InfluxConnectionUrl() {
        final var builder = new StringBuilder();
        boolean enableSSL = Boolean.parseBoolean(JMeterUtils.getProperty(INFLUX_SSL_ENABLE.key()));
        String hostName = JMeterUtils.getProperty(INFLUX_HOSTNAME.key());
        String port = JMeterUtils.getProperty(INFLUX_PORT.key());

        if (enableSSL) {
            builder.append("https");
        } else {
            builder.append("http");
        }
        builder.append("://");
        builder.append(hostName);
        builder.append(":");
        builder.append(port);

        return builder.toString();
    }

    public String influxToken() {
        return JMeterUtils.getProperty(INFLUX_TOKEN.key());
    }

    public String influxOrganizationName() {
        return JMeterUtils.getProperty(INFLUX_ORGANIZATION.key());
    }

    public String influxBucketName() {
        return JMeterUtils.getProperty(INFLUX_BUCKET.key());
    }

    public String testName() {
        return JMeterUtils.getPropDefault(TEST_NAME.key(), DEFAULT_TEST_NAME);
    }

    public String testRunId() {
        return JMeterUtils.getPropDefault(TEST_RUN_ID.key(), DEFAULT_RUN_ID);
    }

    public boolean saveErrorResponse() {
        return Boolean.parseBoolean(JMeterUtils.getPropDefault(SAVE_ERROR_RESPONSE.key(), "false"));
    }

    public boolean measureSubResult() {
        return Boolean.parseBoolean(JMeterUtils.getPropDefault(MEASURE_SUB_RESULT.key(), "false"));
    }

}
