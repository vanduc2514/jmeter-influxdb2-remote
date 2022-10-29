package com.nttdatavds.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

public class InfluxClientBuilder {

    private String influxConnectionUrl;

    private String influxToken;

    private String influxOrganizationName;

    private String influxBucketName;

    InfluxClientBuilder() {
    }

    public InfluxClientBuilder connectionUrl(String connectionUrl) {
        validateConnectionUrl(connectionUrl);
        influxConnectionUrl = connectionUrl;
        return this;
    }

    public InfluxClientBuilder token(String token) {
        validateStringValue("influxToken", token);
        influxToken = token;
        return this;
    }

    public InfluxClientBuilder organization(String organization) {
        validateStringValue("influxOrganizationName", organization);
        influxOrganizationName = organization;
        return this;
    }

    public InfluxClientBuilder bucket(String bucket) {
        validateStringValue("influxBucketName", bucket);
        influxBucketName = bucket;
        return this;
    }

    public InfluxClient build() throws InfluxClientException {
        InfluxDBClient actualClient = InfluxDBClientFactory.create(
                influxConnectionUrl,
                influxToken.toCharArray(),
                influxOrganizationName,
                influxBucketName
        );
        return new InfluxClient(actualClient, influxConnectionUrl);
    }

    private void validateConnectionUrl(String connectionUrl) {
        boolean validated = connectionUrl.contains("http") || connectionUrl.contains("https");
        if (!validated) {
            throw new IllegalArgumentException("Connection url should contains http or https");
        }
    }

    private void validateStringValue(String name, String value) {
        boolean inValid = value == null || value.isEmpty() || value.isBlank();
        if (inValid) {
            throw new IllegalArgumentException(name + " is null or empty");
        }
    }

    public InfluxClientBuilder writeBatchSize(int writeBatchSize) {
        return null;
    }

    public InfluxClientBuilder writeFlushInterval(int writeFlushInterval) {
        return null;
    }

    public InfluxClientBuilder writeBufferLimit(int writeBufferLimit) {
        return null;
    }
}
