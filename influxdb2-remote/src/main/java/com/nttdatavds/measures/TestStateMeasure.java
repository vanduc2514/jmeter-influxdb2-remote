package com.nttdatavds.measures;

import com.influxdb.client.write.Point;
import com.nttdatavds.measures.metrics.TestStartEndMeasurement;
import com.nttdatavds.measures.metrics.VirtualUsersMeasurement;
import org.apache.jmeter.visualizers.backend.UserMetric;

public interface TestStateMeasure {

    /**
     * Write the {@link TestStartEndMeasurement} metric to Influx Database.
     * This method write the Influx {@link Point} data about the start of a test plan
     */
    void writeStartState();

    /**
     * Write the {@link TestStartEndMeasurement} metric to Influx Database.
     * This method write the Influx {@link Point} data about the finish of a test plan
     */
    void writeFinishState();

    /**
     * Write the {@link VirtualUsersMeasurement} metric to Influx Database.
     *
     * @param userMetric the Jmeter {@link UserMetric}
     */
    void writeUserMetric(UserMetric userMetric);

}
