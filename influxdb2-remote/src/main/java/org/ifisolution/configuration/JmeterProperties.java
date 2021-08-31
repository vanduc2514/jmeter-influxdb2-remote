package org.ifisolution.configuration;

public enum JmeterProperties {
    INFLUX_HOSTNAME("influxdb.hostname"),
    INFLUX_PORT("influxdb.port"),
    INFLUX_TOKEN("influxdb.token"),
    INFLUX_ORGANIZATION("influxdb.organization"),
    INFLUX_BUCKET("influxdb.bucket"),
    INFLUX_SSL_ENABLE("influxdb.ssl.enable"),
    TEST_NAME("test.name"),
    TEST_RUN_ID("test.runId");

    private final String key;

    JmeterProperties(String name) {
        this.key = name;
    }

    public String key() {
        return key;
    }

}