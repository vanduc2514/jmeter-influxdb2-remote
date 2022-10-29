package com.nttdatavds.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class InfluxClientProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxClientProxy.class);

    private final InfluxDBClient actualClient;

    private final WriteApi singletonWriteApi;

    private final String url;

    public static InfluxClientProxyBuilder builder() {
        return new InfluxClientProxyBuilder();
    }

    InfluxClientProxy(InfluxDBClient actualClient,
                      String influxConnectionUrl,
                      int writeBatchSize,
                      int writeFlushInterval,
                      int writeBufferLimit) throws InfluxClientException {
        this.actualClient = actualClient;
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(writeBatchSize)
                .flushInterval(writeFlushInterval)
                .bufferLimit(writeBufferLimit)
                .build();
        this.singletonWriteApi = actualClient.makeWriteApi(writeOptions);
        this.url = influxConnectionUrl;
        checkHealth();
    }

    private void checkHealth() throws InfluxClientException {
        LOGGER.info("Health Check to {}", url);
        HealthCheck health = actualClient.health();
        HealthCheck.StatusEnum healthStatus = health.getStatus();
        if (healthStatus == null ||
                healthStatus == HealthCheck.StatusEnum.FAIL) {
            String pattern = "Health Check fails. {0}";
            throw new InfluxClientException(
                    MessageFormat.format(pattern, url, health.getMessage())
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

}
