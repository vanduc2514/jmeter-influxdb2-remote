package org.ifisolution.measures;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.ifisolution.influxdb.configuration.JmeterPropertiesProvider;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.ifisolution.measures.metrics.RequestMeasurement;
import org.ifisolution.measures.metrics.TestStartEndMeasurement;
import org.ifisolution.util.MeasureUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InfluxMeasureImpl implements InfluxMeasure {

    private static InfluxMeasureImpl manager;

    private final InfluxClient influxClient;

    private String hostName;

    private String testName;

    private String runId;

    private boolean saveErrorResponse;

    private InfluxMeasureImpl(InfluxClient influxClient) {
        this.influxClient = influxClient;
    }

    /**
     * Singleton Default Manager
     *
     * @return the {@link InfluxMeasureImpl}
     */
    public static InfluxMeasureImpl getInstance() {
        if (manager == null) {
            JmeterPropertiesProvider jmeterPropertiesProvider = new JmeterPropertiesProvider();
            InfluxClient influxClient = InfluxClient.buildClient(
                    new InfluxClientConfiguration(jmeterPropertiesProvider)
            );
            manager = new InfluxMeasureImpl(influxClient);
        }
        try {
            manager.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            manager.hostName = "Unknown Host";
        }
        return manager;
    }

    public void setSaveErrorResponse(boolean saveErrorResponse) {
        this.saveErrorResponse = saveErrorResponse;
    }

    public boolean isSaveErrorResponse() {
        return saveErrorResponse;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getRunId() {
        return runId;
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

    @Override
    public void writeTestResult(SampleEvent sampleEvent) {
        SampleResult sampleResult = sampleEvent.getResult();
        long latency = sampleResult.getLatency();
        long connectTime = sampleResult.getConnectTime();
        String failureMessage = sampleResult.getFirstAssertionFailureMessage();
        boolean errorOccurred = failureMessage != null;

        Point point = Point.measurement(RequestMeasurement.MEASUREMENT_NAME);

        if (errorOccurred) {
            point.addTag(RequestMeasurement.Tags.ERROR_MSG, failureMessage);
            if (saveErrorResponse) {
                String errorBody = sampleResult.getResponseDataAsString();
                if (errorBody != null && !errorBody.isEmpty()) {
                    errorBody = getEscapedString(errorBody);
                } else {
                    errorBody = "Error Body is empty";
                }
                point.addTag(RequestMeasurement.Tags.ERROR_RESPONSE_BODY, errorBody);
            }
        }

        point.time(MeasureUtil.getCurrentTimeNanoSecond(), WritePrecision.NS)
                .addTag(RequestMeasurement.Tags.TEST_NAME, testName)
                .addTag(RequestMeasurement.Tags.RUN_ID, runId)
                .addTag(RequestMeasurement.Tags.REQUEST_NAME, sampleResult.getSampleLabel())
                .addTag(RequestMeasurement.Tags.NODE_NAME, hostName)
                .addTag(RequestMeasurement.Tags.RESULT_CODE, sampleResult.getResponseCode())
                .addField(RequestMeasurement.Fields.ERROR_COUNT, sampleResult.getErrorCount())
                .addField(RequestMeasurement.Fields.REQUEST_COUNT, sampleResult.getSampleCount())
                .addField(RequestMeasurement.Fields.RECEIVED_BYTES, sampleResult.getBytesAsLong())
                .addField(RequestMeasurement.Fields.SENT_BYTES, sampleResult.getSentBytes())
                .addField(RequestMeasurement.Fields.RESPONSE_TIME, sampleResult.getTime())
                .addField(RequestMeasurement.Fields.LATENCY, latency)
                .addField(RequestMeasurement.Fields.CONNECT_TIME, connectTime)
                .addField(RequestMeasurement.Fields.PROCESSING_TIME, latency - connectTime);

        this.influxClient.writeInfluxPoint(point);
    }

    /**
     * Updates not supported values.
     * @param value the string which is going to be updated.
     * @return the escaped string.
     */
    private String getEscapedString(String value) {
        return value.replace("\n", "")
                .replace("\r", "")
                .replace(" ", "\\ ")
                .replace(",", ",\\ ")
                .replace("=", "=\\ ");
    }

    @Override
    public void close() {
        this.influxClient.closeClient();
        manager = null;
    }

}
