package org.ifisolution.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.measures.InfluxTestStateMeasure;
import org.ifisolution.measures.metrics.TestStartEndMeasurement;
import org.ifisolution.measures.metrics.VirtualUsersMeasurement;
import org.ifisolution.util.MeasureUtil;

public class InfluxTestStateMeasureImpl extends AbstractInfluxMeasure implements InfluxTestStateMeasure {

    private static InfluxTestStateMeasureImpl measure;

    public static InfluxTestStateMeasureImpl getInstance() {
        if (measure == null) {
            InfluxPropertiesProvider jmeterPropertiesProvider = new InfluxPropertiesProvider();
            InfluxClient influxClient = InfluxClient.buildClient(
                    new InfluxClientConfiguration(jmeterPropertiesProvider)
            );
            measure = new InfluxTestStateMeasureImpl(influxClient);
        }
        return measure;
    }

    private InfluxTestStateMeasureImpl(InfluxClient influxClient) {
        super(influxClient);
    }

    @Override
    public void writeStartState() {
        Point startPoint = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME)
                .time(MeasureUtil.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.STARTED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, hostName)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, testName)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");
        this.influxClient.writeInfluxPoint(startPoint);
    }

    @Override
    public void writeFinishState() {
        Point finishPoint = Point.measurement(TestStartEndMeasurement.MEASUREMENT_NAME)
                .time(MeasureUtil.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addTag(TestStartEndMeasurement.Tags.TYPE, TestStartEndMeasurement.Values.FINISHED)
                .addTag(TestStartEndMeasurement.Tags.NODE_NAME, hostName)
                .addTag(TestStartEndMeasurement.Tags.TEST_NAME, testName)
                .addTag(TestStartEndMeasurement.Tags.RUN_ID, runId)
                .addField(TestStartEndMeasurement.Fields.PLACEHOLDER, "1");
        this.influxClient.writeInfluxPoint(finishPoint);
    }

    @Override
    public void writeUserMetric(UserMetric userMetric) {
        JMeterContextService.ThreadCounts tc = JMeterContextService.getThreadCounts();
        Point userPoint = Point.measurement(VirtualUsersMeasurement.MEASUREMENT_NAME)
                .time(MeasureUtil.getCurrentTimeMilliSecond(), WritePrecision.MS)
                .addField(VirtualUsersMeasurement.Fields.MIN_ACTIVE_THREADS, userMetric.getMinActiveThreads())
                .addField(VirtualUsersMeasurement.Fields.MAX_ACTIVE_THREADS, userMetric.getMaxActiveThreads())
                .addField(VirtualUsersMeasurement.Fields.MEAN_ACTIVE_THREADS, userMetric.getMeanActiveThreads())
                .addField(VirtualUsersMeasurement.Fields.STARTED_THREADS, tc.startedThreads)
                .addField(VirtualUsersMeasurement.Fields.FINISHED_THREADS, tc.finishedThreads)
                .addTag(VirtualUsersMeasurement.Tags.NODE_NAME, this.hostName)
                .addTag(VirtualUsersMeasurement.Tags.TEST_NAME, this.testName)
                .addTag(VirtualUsersMeasurement.Tags.RUN_ID, this.runId);
        this.influxClient.writeInfluxPoint(userPoint);
    }
}
