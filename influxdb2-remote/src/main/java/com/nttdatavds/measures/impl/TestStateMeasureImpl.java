package com.nttdatavds.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.nttdatavds.influxdb.InfluxClientProxy;
import com.nttdatavds.measures.MeasureHelper;
import com.nttdatavds.measures.TestStateMeasure;
import com.nttdatavds.measures.metrics.TestStartEndMeasurement;
import com.nttdatavds.measures.metrics.VirtualUsersMeasurement;
import org.apache.jmeter.visualizers.backend.UserMetric;

public class TestStateMeasureImpl extends AbstractInfluxMeasure implements TestStateMeasure {

    private TestStateMeasureImpl(InfluxClientProxy influxClientProxy) {
        super(influxClientProxy);
    }

    @Override
    public void writeStartState() {
        Point startPoint = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME)
                .time(MeasureHelper.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.STARTED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, hostName)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, testName)
                .addTag(TestStartEndMeasurement.Tags.RUN_ID, runId)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");
        this.influxClientProxy.writeInfluxPoint(startPoint);
    }

    @Override
    public void writeFinishState() {
        Point finishPoint = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME)
                .time(MeasureHelper.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.FINISHED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, hostName)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, testName)
                .addTag(TestStartEndMeasurement.Tags.RUN_ID, runId)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");
        this.influxClientProxy.writeInfluxPoint(finishPoint);
    }

    @Override
    public void writeUserMetric(UserMetric userMetric) {
        Point userPoint = Point.measurement(VirtualUsersMeasurement.MEASUREMENT_NAME)
                .time(MeasureHelper.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addField(VirtualUsersMeasurement.Fields.STARTED_THREADS, userMetric.getStartedThreads())
                .addField(VirtualUsersMeasurement.Fields.FINISHED_THREADS, userMetric.getFinishedThreads())
                .addTag(VirtualUsersMeasurement.Tags.NODE_NAME, this.hostName)
                .addTag(VirtualUsersMeasurement.Tags.TEST_NAME, this.testName)
                .addTag(VirtualUsersMeasurement.Tags.RUN_ID, this.runId);
        this.influxClientProxy.writeInfluxPoint(userPoint);
    }

    public static class TestStateMeasureBuilder extends
            AbstractInfluxMeasureBuilder<TestStateMeasureBuilder, TestStateMeasureImpl> {

        public TestStateMeasureBuilder(InfluxClientProxy influxClientProxy) {
            super(new TestStateMeasureImpl(influxClientProxy));
        }

        @Override
        protected TestStateMeasureBuilder builder() {
            return this;
        }

    }
}
