package org.ifisolution.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.jmeter.samplers.SampleResult;
import org.ifisolution.configuration.MeasureSettings;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.measures.MeasureHelper;
import org.ifisolution.measures.TestResultMeasure;
import org.ifisolution.measures.metrics.RequestMeasurement;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestResultMeasureImpl extends AbstractInfluxMeasure implements TestResultMeasure {

    private final AtomicBoolean isClientClosed;

    private final boolean saveErrorResponse;

    private final boolean measureSubResult;

    public TestResultMeasureImpl(InfluxClient influxClient,
                                       MeasureSettings measureSettings) {
        super(influxClient, measureSettings);
        isClientClosed = new AtomicBoolean(false);
        measureSubResult = measureSettings.isMeasureSubResult();
        saveErrorResponse = measureSettings.isSaveErrorResponse();
    }

    public TestResultMeasureImpl(String hostName,
                                 String testName,
                                 String runId,
                                 boolean saveErrorResponse,
                                 boolean measureSubResult,
                                 InfluxClient influxClient) {
        super(hostName, testName, runId, influxClient);
        this.saveErrorResponse = saveErrorResponse;
        this.measureSubResult = measureSubResult;
        isClientClosed = new AtomicBoolean(false);
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

        resultPoint.time(MeasureHelper.getCurrentTimeNanoSecond(), WritePrecision.NS)
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

        if (measureSubResult) {
            SampleResult[] subResults = sampleResult.getSubResults();
            if (subResults == null || subResults.length == 0) return; //Break from recursion
            for (SampleResult subResult : subResults) {
                writeTestResult(subResult);
            }
        }

    }

    private boolean hasErrorResponseCode(SampleResult sampleResult) {
        char responseCategory = sampleResult.getResponseCode().charAt(0);
        return responseCategory == '4' || responseCategory == '5';
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
