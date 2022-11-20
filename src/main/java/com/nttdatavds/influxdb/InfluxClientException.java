package com.nttdatavds.influxdb;

/**
 * Custom Exception when sending data to InfluxDB
 */
public class InfluxClientException extends Exception {

    public InfluxClientException(Throwable cause) {
        super(cause);
    }

    public InfluxClientException(String message) {
        super(message);
    }

}
