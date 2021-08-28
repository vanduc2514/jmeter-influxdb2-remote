package org.ifisolution.influxdb;

import org.ifisolution.configuration.InfluxConfigurationProvider;

public class InfluxClientConfiguration {

    private final InfluxConfigurationProvider influxConfigurationProvider;

    public InfluxClientConfiguration(InfluxConfigurationProvider influxConfigurationProvider) {
        this.influxConfigurationProvider = influxConfigurationProvider;
    }

    public String getConnectionUrl() {
        String connectionUrl = influxConfigurationProvider.provideConnectionUrl();
        if (validateConnectionUrl(connectionUrl)) {
            return connectionUrl;
        }
        return null;
    }

    private boolean validateConnectionUrl(String connectionUrl) {
        return connectionUrl.contains("http") || connectionUrl.contains("https");
    }

    public char[] getToken() {
        String token = influxConfigurationProvider.provideToken();
        if (validateToken(token)) {
            return token.toCharArray();
        }
        return null;
    }

    private boolean validateToken(String token) {
        return true;
    }

    public String getOrganization() {
        return influxConfigurationProvider.provideOrganizationName();
    }

    public String getBucketName() {
        return influxConfigurationProvider.provideBucketName();
    }
}
