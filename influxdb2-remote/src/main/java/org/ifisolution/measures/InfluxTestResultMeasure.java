package org.ifisolution.measures;

import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.ifisolution.measures.metrics.RequestMeasurement;

public interface InfluxTestResultMeasure extends InfluxMeasure {

    /**
     * Write {@link RequestMeasurement} metric to Influx Database
     *
     * @param sampleEvent the {@link SampleEvent} captured by Jmeter {@link RemoteSampleListener}
     */
    void writeTestResult(SampleEvent sampleEvent);

}
