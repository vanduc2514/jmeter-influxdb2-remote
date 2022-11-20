package com.nttdatavds.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proxy for {@link InfluxDBClient} which exposes methods for interacting with InfluxDB
 */
public class InfluxClientProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxClientProxy.class);

    private final InfluxDBClient actualClient;


    private final WriteApi singletonWriteApi;

    private InfluxClientProxy(InfluxDBClient actualClient,
                              WriteOptions writeOptions) {
        this.actualClient = actualClient;
        this.singletonWriteApi = actualClient.makeWriteApi(writeOptions);
    }

    /**
     * Make a new proxy instance.
     *
     * @throws InfluxClientException if the Influx Database cannot be accessed.
     */
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
        ).enableGzip();
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(writeBatchSize)
                .flushInterval(writeFlushInterval)
                .bufferLimit(writeBufferLimit)
                .build();
        final InfluxClientProxy INSTANCE = new InfluxClientProxy(influxClient, writeOptions);
        INSTANCE.checkConnection();
        return INSTANCE;
    }

    private void checkConnection() throws InfluxClientException {
        LOGGER.info("Attempt to ping Influx Database.");
        Boolean alive = actualClient.ping();
        if (Boolean.FALSE.equals(alive)) {
            throw new InfluxClientException("Influx Database cannot be accessed!");
        }
        String influxVersion = actualClient.version();
        LOGGER.info("Influx Database version {} is accessible", influxVersion);
    }

    /**
     * Write values to influx Database
     *
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

    /**
     * Flush all remaining request that needs to be sent to InfluxDB and close
     * the actual client.
     *
     * @throws InfluxClientException if the request cannot be flushed or the client
     * cannot be closed.
     */
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
