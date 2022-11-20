package com.nttdatavds.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Class to acquire Configuration for this Plugin from <b>.properties</b> file
 * or from the properties given by the command line option using {@link JMeterUtils}.
 *
 * @see <a href='https://jmeter.apache.org/usermanual/get-started.html#server'>
 *          https://jmeter.apache.org/usermanual/get-started.html#server
 *     </a>
 * @see <a href='https://jmeter.apache.org/usermanual/get-started.html#override'>
 *          https://jmeter.apache.org/usermanual/get-started.html#override
 *      </a>
 */
public class PluginConfiguration {

    public static final String DEFAULT_TEST_NAME = "Jmeter_TestPlan_Default";
    public static final String DEFAULT_RUN_ID = "R001";
    public static final String EMPTY = StringUtils.EMPTY;

    private PluginConfiguration() throws IllegalAccessException {
        throw new IllegalAccessException("Utility class can't be instantiated");
    }

    /**
     * @return the InfluxDB connection String with format {@code scheme://hostname:port}
     * Default value is {@link #EMPTY}
     */
    public static String influxConnectionUrl() {
        String url = StringUtils.EMPTY;
        boolean enableSSL = JMeterUtils.getPropDefault("influxdb.ssl.enable", false);
        String hostName = JMeterUtils.getPropDefault("influxdb.hostname", EMPTY);
        String port = JMeterUtils.getPropDefault("influxdb.port", EMPTY);
        return url + (enableSSL ? "https" : "http") + "://" + hostName + ":" + port;
    }

    /**
     * @return the InfluxDB Organization Name. Default value is {@link #EMPTY}
     */
    public static String influxOrganizationName() {
        return JMeterUtils.getPropDefault("influxdb.organization", EMPTY);
    }

    /**
     * @return the InfluxDB Bucket Name. Default value is {@link #EMPTY}
     */
    public static String influxBucketName() {
        return JMeterUtils.getPropDefault("influxdb.bucket", EMPTY);
    }

    /**
     * @return access token to connect InfluxDB, converted to {@link char[]}.
     * Default value is empty array.
     */
    public static char[] influxTokenNew() {
        return JMeterUtils.getPropDefault("influxdb.token", EMPTY).toCharArray();
    }

    @Deprecated
    public static String influxToken() {
        return JMeterUtils.getPropDefault("influxdb.token", EMPTY);
    }

    /**
     * @return the Name of the Test Plan. Default value is {@link #EMPTY}
     */
    public static String testName() {
        return JMeterUtils.getPropDefault("test.name", DEFAULT_TEST_NAME);
    }

    /**
     * @return the RunId of the Test Plan. Default value is {@link #EMPTY}
     */
    public static String testRunId() {
        return JMeterUtils.getPropDefault("test.runId", DEFAULT_RUN_ID);
    }

    /**
     * @return true if the Plugin is allowed to send the response of error request to InfluxDB.
     * Default value is {@code false}
     */
    public static boolean saveErrorResponse() {
        return JMeterUtils.getPropDefault("measure.save.error", false);
    }

    /**
     * @return true if the Plugin is allowed to send the sub-result of request to InfluxDB.
     * Default value is {@code false}
     */
    public static boolean measureSubResult() {
        return JMeterUtils.getPropDefault("measure.sub.result", false);
    }

    /**
     * @return the second which the User Metric Result should be sent to InfluxDB.
     * Default value is {@code 1}
     */
    public static int userMetricInterval() {
        return JMeterUtils.getPropDefault("measure.user.interval", 1);
    }

    /**
     * @return the size of the Thread pool used for sending User Metric Result. The more of
     * size of it, the more concurrent result it can send to InfluxDB. Default value is {@code 10}
     */
    public static int userMetricPoolSize() {
        return JMeterUtils.getPropDefault("measure.user.pool", 10);
    }

    /**
     * @return the size of the batch that {@link com.influxdb.client.InfluxDBClient} uses
     * for batching request send to InfluxDB. Default value is {@code 1000}
     */
    public static int writeBatchSize() {
        return JMeterUtils.getPropDefault("write.batch", 1000);
    }

    /**
     * @return the second which each request to InfluxDB sent by {@link com.influxdb.client.InfluxDBClient}
     * Default value is {@code 1000}
     */
    public static int writeFlushInterval() {
        return JMeterUtils.getPropDefault("write.flush.interval", 1000);
    }

    /**
     * @return the size of the buffer that {@link com.influxdb.client.InfluxDBClient} uses for
     * sending request to InfluxDB. Default value is {@code 10000}
     */
    public static int writeBufferLimit() {
        return JMeterUtils.getPropDefault("write.buffer.limit", 10000);
    }
}
