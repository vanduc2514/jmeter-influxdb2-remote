package org.ifisolution.measures;

import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.ifisolution.measures.metrics.RequestMeasurement;

public interface InfluxTestResultMeasure extends InfluxMeasure {

    /**
     * Write {@link RequestMeasurement} metric to Influx Database
     *
     * @param sampleEvent the {@link SampleResult} captured by Jmeter {@link RemoteSampleListener}
     */
    void writeTestResult(SampleResult sampleEvent);

    /**
     * @return true if the {@link MeasureConfigurationProvider#measureSubResult()} property is set
     */
    boolean measureSubResult();

}
