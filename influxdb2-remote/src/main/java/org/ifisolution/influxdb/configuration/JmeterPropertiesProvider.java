package org.ifisolution.influxdb.configuration;

import org.apache.jmeter.util.JMeterUtils;

public class JmeterPropertiesProvider implements InfluxConfigurationProvider {

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

}
