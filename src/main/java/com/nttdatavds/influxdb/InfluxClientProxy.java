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

    public static InfluxClientProxy getInstance(String influxConnectionUrl,
                                                String influxToken,
                                                String influxOrganizationName,
                                                String influxBucketName,
                                                int writeBatchSize,
                                                int writeFlushInterval,
                                                int writeBufferLimit) throws InfluxClientException {
        return new InfluxClientProxy(
                influxConnectionUrl,
                influxToken,
                influxOrganizationName,
                influxBucketName,
                writeBatchSize,
                writeFlushInterval,
                writeBufferLimit);
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
        InfluxDBClient influxClient = InfluxDBClientFactory.create(
                this.influxConnectionUrl,
                this.influxToken.toCharArray(),
                this.influxOrganizationName,
                this.influxBucketName
        );
        this.actualClient = influxClient;
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(this.writeBatchSize)
                .flushInterval(this.writeFlushInterval)
                .bufferLimit(this.writeBufferLimit)
                .build();
        this.singletonWriteApi = influxClient.makeWriteApi(writeOptions);
        checkHealth();
    }

    private void checkHealth() throws InfluxClientException {
        LOGGER.info("Health Check to Influx Database at {}", influxConnectionUrl);
        Boolean health = actualClient.ping();
        if (Boolean.FALSE.equals(health)) {
            String pattern = "Health Check to Influx Database fails at {0}";
            throw new InfluxClientException(MessageFormat.format(pattern, influxConnectionUrl));
        }
        String influxVersion = actualClient.version();
        LOGGER.info("Influx Database version {} has good health", influxVersion);
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

}
