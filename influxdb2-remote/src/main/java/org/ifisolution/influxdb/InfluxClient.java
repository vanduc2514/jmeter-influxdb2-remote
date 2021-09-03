package org.ifisolution.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.InfluxException;
import org.ifisolution.exeptions.ClientValidationException;
import org.ifisolution.exeptions.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

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

    public static InfluxClient buildClient(InfluxClientConfiguration clientConfiguration) throws PluginException {
        InfluxDBClient influxDBClient;
        try {
            influxDBClient = InfluxDBClientFactory.create(
                    clientConfiguration.getConnectionUrl(),
                    clientConfiguration.getToken(),
                    clientConfiguration.getOrganization(),
                    clientConfiguration.getBucketName()
            );
        } catch (ClientValidationException e) {
            throw new PluginException(e);
        }
        InfluxClient influxClient = new InfluxClient(influxDBClient);
        LOGGER.info("Executing Initial Health Check to {}", influxDBClient.toString());
        influxClient.healthCheck();
        return influxClient;
    }

    private void healthCheck() throws PluginException {
        HealthCheck health = actualClient.health();
        HealthCheck.StatusEnum healthStatus = health.getStatus();
        if (healthStatus == null ||
                healthStatus == HealthCheck.StatusEnum.FAIL) {
            String pattern = "Health Check fails @ {0}, Reason: {1}";
            throw new PluginException(
                    MessageFormat.format(pattern, actualClient.toString(), health.getMessage())
            );
        }
    }

    /**
     * Write values to influx Database
     * @param point the influxDb {@link Point} wrapper
     */
    public void writeInfluxPoint(Point point) throws InfluxException {
        // Write by Data Point
        singletonWriteApi.writePoint(point);
    }

    public void closeClient() {
        this.actualClient.close();
    }

    public String getHostName() {
        return actualClient.toString();
    }

}
