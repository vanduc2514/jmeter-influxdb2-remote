package org.ifisolution.measures;

import org.apache.jmeter.visualizers.backend.UserMetric;

public interface InfluxTestStateMeasure extends InfluxMeasure {

    void writeStartState();

    void writeFinishState();

    void writeUserMetric(UserMetric userMetric);

}
