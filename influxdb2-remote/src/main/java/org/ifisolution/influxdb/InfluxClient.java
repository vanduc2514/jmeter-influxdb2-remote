package org.ifisolution.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxClient.class);

    private final InfluxDBClient actualClient;

    private final WriteApi singletonWriteApi;

    /**
     * Use factory method {@link #buildClient(InfluxClientConfiguration)}
     */
    private InfluxClient(InfluxDBClient actualClient) {
        this.actualClient = actualClient;
        this.singletonWriteApi = actualClient.makeWriteApi();
    }

    public static InfluxClient buildClient(InfluxClientConfiguration clientConfiguration) {
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(
                clientConfiguration.getConnectionUrl(),
                clientConfiguration.getToken(),
                clientConfiguration.getOrganization(),
                clientConfiguration.getBucketName()
        );
        HealthCheck health = influxDBClient.health();
        HealthCheck.StatusEnum healthStatus = health.getStatus();
        System.out.println("Influx Status: " + healthStatus);
        if (healthStatus == HealthCheck.StatusEnum.FAIL) {
            System.out.println(health.getMessage());
        }
        return new InfluxClient(influxDBClient);
    }

    /**
     * Write values to influx Database
     * @param point the influxDb {@link Point} wrapper
     */
    public void writeInfluxPoint(Point point) throws InfluxException {
        // Write by Data Point
        try {
            singletonWriteApi.writePoint(point);
        } catch (InfluxException e) {
            e.printStackTrace();
        }
    }

    public void closeClient() {
        this.actualClient.close();
    }

}
