package com.nttdatavds.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.nttdatavds.influxdb.InfluxClientProxy;
import com.nttdatavds.measures.MeasureHelper;
import com.nttdatavds.measures.TestResultMeasure;
import com.nttdatavds.measures.metrics.RequestMeasurement;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Optional;
import java.util.function.Function;

public class TestResultMeasureImpl extends AbstractInfluxMeasure implements TestResultMeasure {

    private boolean saveErrorResponse;

    private boolean measureSubResult;

    private TestResultMeasureImpl(InfluxClientProxy influxClientProxy) {
        super(influxClientProxy);
    }

    @Override
    public void writeTestResult(SampleResult sampleResult) {
        Function<Function<SampleResult, String>, String> getString = getStringValue(sampleResult);
        long latency = sampleResult.getLatency();
        long connectTime = sampleResult.getConnectTime();
        String failureMessage = sampleResult.getFirstAssertionFailureMessage();
        boolean errorOccurred = failureMessage != null || hasErrorResponseCode(sampleResult);

        Point resultPoint = Point.measurement(RequestMeasurement.MEASUREMENT_NAME);

        if (errorOccurred) {
            if (failureMessage != null) {
                resultPoint.addTag(RequestMeasurement.Tags.ERROR_MSG, failureMessage);
            }
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
                .addTag(RequestMeasurement.Tags.REQUEST_NAME, getString.apply(SampleResult::getSampleLabel))
                .addTag(RequestMeasurement.Tags.NODE_NAME, hostName)
                .addTag(RequestMeasurement.Tags.RESULT_CODE, getString.apply(SampleResult::getResponseCode))
                .addField(RequestMeasurement.Fields.ERROR_COUNT, sampleResult.getErrorCount())
                .addField(RequestMeasurement.Fields.REQUEST_COUNT, sampleResult.getSampleCount())
                .addField(RequestMeasurement.Fields.RECEIVED_BYTES, sampleResult.getBytesAsLong())
                .addField(RequestMeasurement.Fields.SENT_BYTES, sampleResult.getSentBytes())
                .addField(RequestMeasurement.Fields.RESPONSE_TIME, sampleResult.getTime())
                .addField(RequestMeasurement.Fields.LATENCY, latency)
                .addField(RequestMeasurement.Fields.CONNECT_TIME, connectTime)
                .addField(RequestMeasurement.Fields.PROCESSING_TIME, latency - connectTime);

        this.influxClientProxy.writeInfluxPoint(resultPoint);

        if (measureSubResult) {
            SampleResult[] subResults = sampleResult.getSubResults();
            if (subResults == null || subResults.length == 0) return; //Break from recursion
            for (SampleResult subResult : subResults) {
                writeTestResult(subResult);
            }
        }

    }

    private Function<Function<SampleResult, String>, String> getStringValue(SampleResult sampleResult) {
        return extractor -> Optional.ofNullable(sampleResult)
                .map(extractor)
                .orElse(StringUtils.EMPTY);
    }

    private boolean hasErrorResponseCode(SampleResult sampleResult) {
        try {
            int responseCode = Integer.parseInt(sampleResult.getResponseCode());
            return isErrorCode(responseCode);
        } catch (NumberFormatException exception) {
            // If the response code is a message
            return true;
        }
    }

    private boolean isErrorCode(int responseCode) {
        int category = responseCode / 100;
        return category == 4 || category == 5;
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

        public TestResultMeasureBuilder(InfluxClientProxy influxClientProxy) {
            super(new TestResultMeasureImpl(influxClientProxy));
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
