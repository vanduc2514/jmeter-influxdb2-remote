package org.ifisolution.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class InfluxClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxClient.class);

    private final InfluxDBClient actualClient;

    private final WriteApi singletonWriteApi;

    private final String url;

    public static InfluxClientBuilder builder() {
        return new InfluxClientBuilder();
    }

    InfluxClient(InfluxDBClient actualClient, String url) throws InfluxClientException {
        this.actualClient = actualClient;
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(2000)
                .flushInterval(10000)
                .build();
        this.singletonWriteApi = actualClient.makeWriteApi(writeOptions);
        this.url = url;
        checkHealth();
    }

    private void checkHealth() throws InfluxClientException {
        LOGGER.info("Executing Initial Health Check to {}", getUrl());
        HealthCheck health = actualClient.health();
        HealthCheck.StatusEnum healthStatus = health.getStatus();
        if (healthStatus == null ||
                healthStatus == HealthCheck.StatusEnum.FAIL) {
            String pattern = "Health Check fails. {0}";
            throw new InfluxClientException(
                    MessageFormat.format(pattern, getUrl(), health.getMessage())
            );
        }
        LOGGER.info("Influx Database health status: {}", healthStatus);
    }

    /**
     * Write values to influx Database
     * @param point the influxDb {@link Point} wrapper
     */
    public void writeInfluxPoint(Point point) {
        // Write by Data Point
        try {
            singletonWriteApi.writePoint(point);
        } catch (InfluxException | IllegalArgumentException e) {
            LOGGER.error("Could not write to Influx. Reason: {}", e.getMessage());
        }
    }

    public void closeClient() {
        try {
            singletonWriteApi.flush();
            this.singletonWriteApi.close();
            this.actualClient.close();
        } catch (InfluxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

}
