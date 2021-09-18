package org.ifisolution.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.ifisolution.influxdb.InfluxConfigurationProvider;
import org.ifisolution.measures.impl.MeasureConfigurationProvider;

import static org.ifisolution.configuration.JmeterProperties.*;

public class InfluxPropertiesProvider implements InfluxConfigurationProvider, MeasureConfigurationProvider {

    public static final String DEFAULT_TEST_NAME = "Jmeter_TestPlan_Default";
    public static final String DEFAULT_RUN_ID = "R001";

    @Override
    public String provideConnectionUrl() {
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

    @Override
    public String provideToken() {
        return JMeterUtils.getProperty(INFLUX_TOKEN.key());
    }

    @Override
    public String provideOrganizationName() {
        return JMeterUtils.getProperty(INFLUX_ORGANIZATION.key());
    }

    @Override
    public String provideBucketName() {
        return JMeterUtils.getProperty(INFLUX_BUCKET.key());
    }

    @Override
    public String provideTestName() {
        return JMeterUtils.getPropDefault(TEST_NAME.key(), DEFAULT_TEST_NAME);
    }

    @Override
    public String provideRunId() {
        return JMeterUtils.getPropDefault(TEST_RUN_ID.key(), DEFAULT_RUN_ID);
    }

    @Override
    public String provideHostName() {
        return JMeterUtils.getLocalHostName();
    }

    @Override
    public boolean provideSaveErrorResponseOption() {
        return Boolean.parseBoolean(JMeterUtils.getProperty(SAVE_ERROR_RESPONSE.key()));
    }

    @Override
    public boolean isStandalone() {
        return Boolean.parseBoolean(JMeterUtils.getProperty(MASTER_SEND_RESULT.key()));
    }

    @Override
    public boolean measureSubResult() {
        String property = JMeterUtils.getProperty(MEASURE_SUB_RESULT.key());
        if (StringUtils.isEmpty(property)) {
            return true; // Default if empty or not set will return true
        }
        return Boolean.parseBoolean(property);
    }
}
