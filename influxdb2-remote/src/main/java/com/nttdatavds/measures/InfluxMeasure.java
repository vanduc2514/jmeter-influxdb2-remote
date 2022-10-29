package com.nttdatavds.measures;

public interface InfluxMeasure {

    /**
     * Close the connection to Influx Database
     */
    void closeInfluxConnection();

}
