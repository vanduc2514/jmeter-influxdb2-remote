package org.ifisolution.influxdb;

import org.ifisolution.exeptions.ClientValidationException;

public class InfluxClientConfiguration {

    private final InfluxConfigurationProvider influxConfigurationProvider;

    public InfluxClientConfiguration(InfluxConfigurationProvider influxConfigurationProvider) {
        this.influxConfigurationProvider = influxConfigurationProvider;
    }

    public String getConnectionUrl() throws ClientValidationException {
        String connectionUrl = influxConfigurationProvider.provideConnectionUrl();
        validateConnectionUrl(connectionUrl);
        return connectionUrl;
    }

    private void validateConnectionUrl(String connectionUrl) throws ClientValidationException {
        boolean validated = connectionUrl.contains("http") || connectionUrl.contains("https");
        if (!validated) {
            throw new ClientValidationException("Connection url should contains http or https");
        }
    }

    public char[] getToken() throws ClientValidationException {
        String token = influxConfigurationProvider.provideToken();
        validateToken(token);
        return token.toCharArray();
    }

    private void validateToken(String token) throws ClientValidationException {
        boolean validated = token != null;
        if (!validated) {
            throw new ClientValidationException("Token is null");
        }
    }

    public String getOrganization() {
        return influxConfigurationProvider.provideOrganizationName();
    }

    public String getBucketName() {
        return influxConfigurationProvider.provideBucketName();
    }
}
