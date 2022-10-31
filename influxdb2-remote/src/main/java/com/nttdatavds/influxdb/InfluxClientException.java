package com.nttdatavds.influxdb;

public class InfluxClientException extends Exception {

    public InfluxClientException(Throwable cause) {
        super(cause);
    }

    public InfluxClientException(String message) {
        super(message);
    }

}
