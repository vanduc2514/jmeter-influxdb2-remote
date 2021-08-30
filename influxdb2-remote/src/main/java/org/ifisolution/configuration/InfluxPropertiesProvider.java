package org.ifisolution.configuration;

import org.apache.jmeter.util.JMeterUtils;
import org.ifisolution.influxdb.InfluxConfigurationProvider;

public class InfluxPropertiesProvider implements InfluxConfigurationProvider {

    @Override
    public String provideConnectionUrl() {
        final var builder = new StringBuilder();
        var enableSSL = Boolean.parseBoolean(JMeterUtils.getProperty(JmeterInfluxProperties.INFLUX_SSL_ENABLE));
        var hostName = JMeterUtils.getProperty(JmeterInfluxProperties.INFLUX_HOSTNAME);
        var port = JMeterUtils.getProperty(JmeterInfluxProperties.INFLUX_PORT);

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
        return JMeterUtils.getProperty(JmeterInfluxProperties.INFLUX_TOKEN);
    }

    @Override
    public String provideOrganizationName() {
        return JMeterUtils.getProperty(JmeterInfluxProperties.INFLUX_ORGANIZATION);
    }

    @Override
    public String provideBucketName() {
        return JMeterUtils.getProperty(JmeterInfluxProperties.INFLUX_BUCKET_NAME);
    }

}
