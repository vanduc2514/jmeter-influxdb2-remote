package com.nttdatavds.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class InfluxClientProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxClientProxy.class);

    private final String influxConnectionUrl;

    private final InfluxDBClient actualClient;

    private final WriteApi singletonWriteApi;

    private InfluxClientProxy(InfluxDBClient actualClient,
                              WriteOptions writeOptions,
                              String influxConnectionUrl) {
        this.actualClient = actualClient;
        this.singletonWriteApi = actualClient.makeWriteApi(writeOptions);
        this.influxConnectionUrl = influxConnectionUrl;
    }

    public static InfluxClientProxy make(String influxConnectionUrl,
                                         char[] influxToken,
                                         String influxOrganizationName,
                                         String influxBucketName,
                                         int writeBatchSize,
                                         int writeFlushInterval,
                                         int writeBufferLimit) throws InfluxClientException {
        InfluxDBClient influxClient = InfluxDBClientFactory.create(
                influxConnectionUrl,
                influxToken,
                influxOrganizationName,
                influxBucketName
        );
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(writeBatchSize)
                .flushInterval(writeFlushInterval)
                .bufferLimit(writeBufferLimit)
                .build();
        final InfluxClientProxy INSTANCE = new InfluxClientProxy(
                influxClient, writeOptions, influxConnectionUrl);
        INSTANCE.checkHealth();
        return INSTANCE;
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
