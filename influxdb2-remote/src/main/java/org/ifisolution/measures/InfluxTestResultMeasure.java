package org.ifisolution.measures;

import org.apache.jmeter.samplers.SampleEvent;

public interface InfluxTestResultMeasure {

    void writeTestResult(SampleEvent sampleEvent);

    void setSaveErrorResponse(boolean saveErrorResponse);

    void close();

    void configureMeasure();

}
