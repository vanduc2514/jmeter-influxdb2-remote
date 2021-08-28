package org.ifisolution.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.InfluxTestStateMeasure;
import org.ifisolution.measures.metrics.TestStartEndMeasurement;
import org.ifisolution.util.MeasureUtil;

public class InfluxTestStateMeasureImpl extends AbstractInfluxMeasure implements InfluxTestStateMeasure {

    public InfluxTestStateMeasureImpl(InfluxClient influxClient) {
        super(influxClient);
    }

    @Override
    public void writeStartState() {
        Point point = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME)
                .time(MeasureUtil.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.STARTED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, hostName)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, testName)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");
        this.influxClient.writeInfluxPoint(point);
    }

    @Override
    public void writeFinishState() {
        Point point = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME)
                .time(MeasureUtil.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.FINISHED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, hostName)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, testName)
                .addTag(TestStartEndMeasurement.Tags.RUN_ID, runId)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");
        this.influxClient.writeInfluxPoint(point);
    }

}
