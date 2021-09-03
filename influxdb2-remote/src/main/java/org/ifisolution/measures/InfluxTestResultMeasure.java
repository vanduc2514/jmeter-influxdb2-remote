package org.ifisolution.measures;

import org.apache.jmeter.samplers.SampleEvent;

public interface InfluxTestResultMeasure extends InfluxMeasure {

    void writeTestResult(SampleEvent sampleEvent);

}
