package com.nttdatavds.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
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

    private final String influxConnectionUrl;

    private final String influxToken;

    private final String influxOrganizationName;

    private final String influxBucketName;

    private final int writeBatchSize;

    private final int writeFlushInterval;

    private final int writeBufferLimit;

    private final InfluxDBClient actualClient;

    private final WriteApi singletonWriteApi;

    private static volatile InfluxClientProxy INSTANCE;

    private static final Object LOCK = new Object();

    public static InfluxClientProxy getInstance(String influxConnectionUrl,
                                                String influxToken,
                                                String influxOrganizationName,
                                                String influxBucketName,
                                                int writeBatchSize,
                                                int writeFlushInterval,
                                                int writeBufferLimit) throws InfluxClientException {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new InfluxClientProxy(
                            influxConnectionUrl,
                            influxToken,
                            influxOrganizationName,
                            influxBucketName,
                            writeBatchSize,
                            writeFlushInterval,
                            writeBufferLimit);
                } else if (INSTANCE.hasDifferentConfiguration(influxConnectionUrl,
                        influxToken,
                        influxOrganizationName,
                        influxBucketName,
                        writeBatchSize,
                        writeFlushInterval,
                        writeBufferLimit)) {
                    INSTANCE = new InfluxClientProxy(
                            influxConnectionUrl,
                            influxToken,
                            influxOrganizationName,
                            influxBucketName,
                            writeBatchSize,
                            writeFlushInterval,
                            writeBufferLimit);
                }
            }
        }
        return INSTANCE;
    }

    private InfluxClientProxy(String influxConnectionUrl,
                      String influxToken,
                      String influxOrganizationName,
                      String influxBucketName,
                      int writeBatchSize,
                      int writeFlushInterval,
                      int writeBufferLimit) throws InfluxClientException {
        this.influxConnectionUrl = influxConnectionUrl;
        this.influxToken = influxToken;
        this.influxOrganizationName = influxOrganizationName;
        this.influxBucketName = influxBucketName;
        this.writeBatchSize = writeBatchSize;
        this.writeFlushInterval = writeFlushInterval;
        this.writeBufferLimit = writeBufferLimit;
        InfluxDBClient actualClient = InfluxDBClientFactory.create(
                this.influxConnectionUrl,
                this.influxToken.toCharArray(),
                this.influxOrganizationName,
                this.influxBucketName
        );
        this.actualClient = actualClient;
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(this.writeBatchSize)
                .flushInterval(this.writeFlushInterval)
                .bufferLimit(this.writeBufferLimit)
                .build();
        this.singletonWriteApi = actualClient.makeWriteApi(writeOptions);
        checkHealth();
    }

    private void checkHealth() throws InfluxClientException {
        LOGGER.info("Health Check to Influx Database at {}", influxConnectionUrl);
        HealthCheck health = actualClient.health();
        HealthCheck.StatusEnum healthStatus = health.getStatus();
        if (healthStatus == null ||
                healthStatus == HealthCheck.StatusEnum.FAIL) {
            String pattern = "Health Check to Influx Database fails at {0}";
            throw new InfluxClientException(
                    MessageFormat.format(pattern, influxConnectionUrl, health.getMessage())
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

    public void closeClient() throws InfluxClientException {
        try {
            singletonWriteApi.flush();
            this.singletonWriteApi.close();
            this.actualClient.close();
        } catch (InfluxException e) {
            throw new InfluxClientException(e);
        }
    }

    private boolean hasDifferentConfiguration(String influxConnectionUrl,
                                              String influxToken,
                                              String influxOrganizationName,
                                              String influxBucketName,
                                              int writeBatchSize,
                                              int writeFlushInterval,
                                              int writeBufferLimit) {
        return !this.influxConnectionUrl.equals(influxConnectionUrl)
                || !this.influxToken.equals(influxToken)
                || !this.influxOrganizationName.equals(influxOrganizationName)
                || !this.influxBucketName.equals(influxBucketName)
                || !(this.writeBatchSize == writeBatchSize)
                || !(this.writeFlushInterval == writeFlushInterval)
                || !(this.writeBufferLimit == writeBufferLimit);
    }

}
