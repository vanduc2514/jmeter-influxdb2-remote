package org.ifisolution.configuration;

import org.apache.jmeter.util.JMeterUtils;
import org.ifisolution.influxdb.InfluxConfigurationProvider;
import org.ifisolution.measures.impl.MeasureConfigurationProvider;

public class InfluxPropertiesProvider implements InfluxConfigurationProvider, MeasureConfigurationProvider {

    public static final String DEFAULT_TEST_NAME = "Jmeter_TestPlan_Default";
    public static final String DEFAULT_RUN_ID = "R001";

    @Override
    public String provideConnectionUrl() {
        final var builder = new StringBuilder();
        var enableSSL = Boolean.parseBoolean(JMeterUtils.getProperty(JmeterProperties.INFLUX_SSL_ENABLE));
        var hostName = JMeterUtils.getProperty(JmeterProperties.INFLUX_HOSTNAME);
        var port = JMeterUtils.getProperty(JmeterProperties.INFLUX_PORT);

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
        return JMeterUtils.getProperty(JmeterProperties.INFLUX_TOKEN);
    }

    @Override
    public String provideOrganizationName() {
        return JMeterUtils.getProperty(JmeterProperties.INFLUX_ORGANIZATION);
    }

    @Override
    public String provideBucketName() {
        return JMeterUtils.getProperty(JmeterProperties.INFLUX_BUCKET_NAME);
    }

    @Override
    public String provideTestName() {
        return JMeterUtils.getPropDefault(JmeterProperties.TEST_NAME, DEFAULT_TEST_NAME);
    }

    @Override
    public String provideRunId() {
        return JMeterUtils.getPropDefault(JmeterProperties.TEST_RUN_ID, DEFAULT_RUN_ID);
    }

    @Override
    public String provideHostName() {
        return JMeterUtils.getLocalHostName();
    }
}
