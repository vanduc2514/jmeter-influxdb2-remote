package com.github.vanduc2514.measures;

import com.github.vanduc2514.measures.metrics.RequestMeasurement;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleResult;

public interface TestResultMeasure {

    /**
     * Write {@link RequestMeasurement} metric to Influx Database
     *
     * @param sampleEvent the {@link SampleResult} captured by Jmeter {@link RemoteSampleListener}
     */
    void writeTestResult(SampleResult sampleEvent);

}
