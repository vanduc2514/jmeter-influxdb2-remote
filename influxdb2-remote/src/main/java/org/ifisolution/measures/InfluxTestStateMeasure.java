package org.ifisolution.measures;

import org.apache.jmeter.visualizers.backend.UserMetric;

public interface InfluxTestStateMeasure {

    void writeStartState();

    void writeFinishState();

    void writeUserMetric(UserMetric userMetric);

    void close();

}
