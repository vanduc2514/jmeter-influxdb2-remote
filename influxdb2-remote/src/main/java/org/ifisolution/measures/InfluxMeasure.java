package org.ifisolution.measures;

import org.apache.jmeter.samplers.SampleEvent;

public interface InfluxMeasure {

    void writeStartState();

    void writeFinishState();

    void writeTestResult(SampleEvent sampleEvent);

    void close();

}
