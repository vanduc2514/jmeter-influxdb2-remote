package org.ifisolution.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.jmeter.samplers.SampleResult;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.metrics.RequestMeasurement;
import org.ifisolution.util.MeasureUtil;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

public class InfluxTestResultMeasureImpl extends AbstractInfluxMeasure implements InfluxTestResultMeasure {

    private final AtomicBoolean isClientClosed;

    private boolean saveErrorResponse;

    public InfluxTestResultMeasureImpl(InfluxClient influxClient,
                                       MeasureConfigurationProvider configurationProvider) {
        super(influxClient, configurationProvider);
        isClientClosed = new AtomicBoolean(false);
    }

    public void setSaveErrorResponse(boolean saveErrorResponse) {
        this.saveErrorResponse = saveErrorResponse;
    }

    @Override
    public void writeTestResult(SampleResult sampleResult) {
        long latency = sampleResult.getLatency();
        long connectTime = sampleResult.getConnectTime();
        String failureMessage = sampleResult.getFirstAssertionFailureMessage();
        boolean errorOccurred = failureMessage != null || hasErrorResponseCode(sampleResult);

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

<<<<<<< HEAD
    private boolean hasErrorResponseCode(SampleResult sampleResult) {
        char responseCategory = sampleResult.getResponseCode().charAt(0);
        return responseCategory == '4' || responseCategory == '5';
=======
    private boolean hasSuccessfulOrResponseCode(SampleResult sampleResult) {
        char responseCategory = sampleResult.getResponseCode().charAt(0);
        return responseCategory == '2' || responseCategory == '3';
>>>>>>> 26a2fc5e9118ffe8f2d4c07a3cd878f355005ceb
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

    /**
     * Thread safe and run only once
     */
    @Override
    public void closeInfluxConnection() {
        if (!isClientClosed.getAndSet(true)) {
            super.closeInfluxConnection();
        }
    }

}
