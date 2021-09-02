package org.ifisolution.measures;

import org.apache.jmeter.samplers.SampleEvent;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.measures.impl.InfluxTestResultMeasureImpl;

public interface InfluxTestResultMeasure {

    void writeTestResult(SampleEvent sampleEvent);

    void setSaveErrorResponse(boolean saveErrorResponse);

    void close();

    /**
     * Configure the {@link InfluxTestResultMeasureImpl} with the Jmeter properties.
     *
     * This operation is Idempotent, which means it will run only once among other threads.
     * If the underlying {@link InfluxClient} is configured by one thread, this method will
     * exit immediately.
     */
    void configureMeasureIdempotent();

}
