package com.nttdatavds.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.nttdatavds.influxdb.InfluxClient;
import com.nttdatavds.measures.MeasureHelper;
import com.nttdatavds.measures.TestResultMeasure;
import com.nttdatavds.measures.metrics.RequestMeasurement;
import org.apache.jmeter.samplers.SampleResult;

public class TestResultMeasureImpl extends AbstractInfluxMeasure implements TestResultMeasure {

    private boolean saveErrorResponse;

    private boolean measureSubResult;

    private TestResultMeasureImpl(InfluxClient influxClient) {
        super(influxClient);
    }

    @Override
    public void writeTestResult(SampleResult sampleResult) {
        long latency = sampleResult.getLatency();
        long connectTime = sampleResult.getConnectTime();
        String failureMessage = sampleResult.getFirstAssertionFailureMessage();
        boolean errorOccurred = failureMessage != null || hasErrorResponseCode(sampleResult);

//        sampleResult.err

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

    public static class TestResultMeasureBuilder extends
            AbstractInfluxMeasureBuilder<TestResultMeasureBuilder, TestResultMeasureImpl> {

        public TestResultMeasureBuilder(InfluxClient influxClient) {
            super(new TestResultMeasureImpl(influxClient));
        }

        public TestResultMeasureBuilder saveErrorResponse(boolean saveErrorResponse) {
            measure.saveErrorResponse = saveErrorResponse;
            return builder();
        }

        public TestResultMeasureBuilder measureSubResult(boolean measureSubResult) {
            measure.measureSubResult = measureSubResult;
            return builder();
        }

        @Override
        protected TestResultMeasureBuilder builder() {
            return this;
        }

    }

}
