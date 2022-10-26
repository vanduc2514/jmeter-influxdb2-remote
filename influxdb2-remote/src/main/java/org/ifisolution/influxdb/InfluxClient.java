package org.ifisolution.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
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

    private final String hostName;

    /**
     * Use factory method {@link #buildClient(InfluxClientConfiguration)}
     */
    private InfluxClient(InfluxDBClient actualClient, String hostName) {
        this.actualClient = actualClient;
        this.hostName = hostName;
        WriteOptions writeOptions = WriteOptions.builder()
                .batchSize(2000)
                .flushInterval(10000)
                .build();
        this.singletonWriteApi = actualClient.makeWriteApi(writeOptions);
    }

    /**
     * Build a {@link InfluxClient} to acquire a connection to Influx Database
     *
     * @param clientConfiguration the {@link InfluxClientConfiguration} contains configuration for this client
     * @return a {@link InfluxClient}
     * @throws PluginException when the {@link InfluxClient} could not establish a connection to Influx
     */
    public static InfluxClient buildClient(InfluxClientConfiguration clientConfiguration) throws PluginException {
        InfluxDBClient influxDBClient;
        InfluxClient influxClient;
        String bucketName = clientConfiguration.getBucketName();
        try {
            String connectionUrl = clientConfiguration.getConnectionUrl();
            influxDBClient = InfluxDBClientFactory.create(
                    connectionUrl,
                    clientConfiguration.getToken(),
                    clientConfiguration.getOrganization(),
                    bucketName
            );
            influxClient = new InfluxClient(influxDBClient, connectionUrl);
        } catch (ClientValidationException | InfluxException e) {
            throw new PluginException(e);
        }
        influxClient.checkHealth();
//        influxClient.verifyBucketAuthorization(bucketName);
        return influxClient;
    }

    private void checkHealth() throws PluginException {
        LOGGER.info("Executing Initial Health Check to {}", getHostName());
        HealthCheck health = actualClient.health();
        HealthCheck.StatusEnum healthStatus = health.getStatus();
        if (healthStatus == null ||
                healthStatus == HealthCheck.StatusEnum.FAIL) {
            String pattern = "Health Check fails. {0}";
            throw new PluginException(
                    MessageFormat.format(pattern, getHostName(), health.getMessage())
            );
        }
        LOGGER.info("Influx Database health status: {}", healthStatus);
    }

//    private void verifyBucketAuthorization(String bucketName) throws PluginException {
//        LOGGER.info("Verifying Authorization to Bucket \"{}\"", bucketName);
//        try {
//            Bucket bucket = actualClient.getBucketsApi().findBucketByName(bucketName);
//            if (bucket == null) {
//                throw new PluginException("Cannot authorize Bucket");
//            }
//            LOGGER.info("Authorized to Influx Bucket {}", bucket.toString().replaceAll("^.*?(?=\\{)", "\n"));
//        } catch (UnauthorizedException e) {
//            throw new PluginException(e);
//        }
//    }

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
            this.actualClient.close();
        } catch (InfluxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public String getHostName() {
        return hostName;
    }

}
