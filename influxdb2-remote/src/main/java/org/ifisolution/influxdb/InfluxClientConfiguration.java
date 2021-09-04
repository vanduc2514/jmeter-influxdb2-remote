package org.ifisolution.influxdb;

import org.ifisolution.exeptions.ClientValidationException;

public class InfluxClientConfiguration {

    private final InfluxConfigurationProvider influxConfigurationProvider;

    public InfluxClientConfiguration(InfluxConfigurationProvider influxConfigurationProvider) {
        this.influxConfigurationProvider = influxConfigurationProvider;
    }

    /**
     * Get the validated connection url
     *
     * @return a connection url string
     * @throws ClientValidationException if the connection url is not valid
     */
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

    /**
     * Get generated token from Influx Database
     *
     * @return the Token Array
     * @throws ClientValidationException if the token is null
     */
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

    /**
     * Get the Organization name from Influx Database
     *
     * @return the Organization name
     */
    public String getOrganization() {
        return influxConfigurationProvider.provideOrganizationName();
    }

    /**
     * Get the Bucket name from Influx Database
     *
     * @return the Bucket name
     */
    public String getBucketName() {
        return influxConfigurationProvider.provideBucketName();
    }
}
