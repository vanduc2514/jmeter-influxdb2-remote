package org.ifisolution.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.metrics.RequestMeasurement;
import org.ifisolution.util.MeasureUtil;

public class InfluxTestResultMeasureImpl extends AbstractInfluxMeasure implements InfluxTestResultMeasure {

    private static InfluxTestResultMeasureImpl measure;

    private boolean saveErrorResponse;

    private InfluxTestResultMeasureImpl(InfluxClient influxClient) {
        super(influxClient);
    }

    /**
     * Singleton Default Manager
     *
     * @return the {@link InfluxTestResultMeasureImpl}
     */
    public static InfluxTestResultMeasureImpl getInstance() {
        if (measure == null) {
            InfluxPropertiesProvider jmeterPropertiesProvider = new InfluxPropertiesProvider();
            InfluxClient influxClient = InfluxClient.buildClient(
                    new InfluxClientConfiguration(jmeterPropertiesProvider)
            );
            measure = new InfluxTestResultMeasureImpl(influxClient);
        }
        return measure;
    }

    public void setSaveErrorResponse(boolean saveErrorResponse) {
        this.saveErrorResponse = saveErrorResponse;
    }

    @Override
    public void writeTestResult(SampleEvent sampleEvent) {
        SampleResult sampleResult = sampleEvent.getResult();
        long latency = sampleResult.getLatency();
        long connectTime = sampleResult.getConnectTime();
        String failureMessage = sampleResult.getFirstAssertionFailureMessage();
        boolean errorOccurred = failureMessage != null;

        Point resultPoint = Point.measurement(RequestMeasurement.MEASUREMENT_NAME);

        if (errorOccurred) {
            resultPoint.addTag(RequestMeasurement.Tags.ERROR_MSG, failureMessage);
            if (saveErrorResponse) {
                String errorBody = sampleResult.getResponseDataAsString();
                if (errorBody != null && !errorBody.isEmpty()) {
                    errorBody = getEscapedString(errorBody);
                } else {
                    errorBody = "Error Body is empty";
                }
                resultPoint.addTag(RequestMeasurement.Tags.ERROR_RESPONSE_BODY, errorBody);
            }
        }

        resultPoint.time(MeasureUtil.getCurrentTimeNanoSecond(), WritePrecision.NS)
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

        this.influxClient.writeInfluxPoint(resultPoint);
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
        super.close();
        measure = null;
    }

}
