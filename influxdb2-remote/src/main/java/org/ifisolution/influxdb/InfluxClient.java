package org.ifisolution.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.write.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxClient.class);

    private final InfluxDBClient actualClient;

    /**
     * Use factory method {@link #buildClient(InfluxClientConfiguration)}
     */
    private InfluxClient(InfluxDBClient actualClient) {
        this.actualClient = actualClient;
    }

    public static InfluxClient buildClient(InfluxClientConfiguration clientConfiguration) {
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(
                clientConfiguration.getConnectionUrl(),
                clientConfiguration.getToken(),
                clientConfiguration.getOrganization(),
                clientConfiguration.getBucketName()
        );
        return new InfluxClient(influxDBClient);
    }

    /**
     * Write values to influx Database
     * @param point the influxDb {@link Point} wrapper
     */
    public void writeInfluxPoint(Point point) {
        // Write by Data Point
        try (WriteApi writeApi = this.actualClient.getWriteApi()) {
            writeApi.writePoint(point);
        } catch (Exception e) {
            LOGGER.error("Failed writing to influx", e);
        }
    }

    public void closeClient() {
        this.actualClient.close();
    }

}
