package org.ifisolution.configuration;

public interface JmeterProperties {

    String INFLUX_HOSTNAME = "influxdb.hostname";

    String INFLUX_PORT = "influxdb.port";

    String INFLUX_SSL_ENABLE = "influxdb.ssl.enable";

    String INFLUX_TOKEN = "influxdb.token";

    String INFLUX_ORGANIZATION = "influxdb.organization";

    String INFLUX_BUCKET_NAME = "influxdb.bucket";

    String TEST_NAME = "test.name";

    String TEST_RUN_ID = "test.runId";

}
