package org.ifisolution.measures.impl;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.metrics.RequestMeasurement;
import org.ifisolution.util.MeasureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class InfluxTestResultMeasureImpl extends AbstractInfluxMeasure implements InfluxTestResultMeasure {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxTestResultMeasureImpl.class);

    private static InfluxTestResultMeasureImpl measure;

    private AtomicBoolean clientConfigured;

    private CountDownLatch clientConfigureLatch;

    private AtomicBoolean isClientClosed;

    private boolean saveErrorResponse;

    private InfluxTestResultMeasureImpl() {
        clientConfigured = new AtomicBoolean(false);
        clientConfigureLatch = new CountDownLatch(1);
    }

    /**
     * Singleton Default Manager
     *
     * @return the {@link InfluxTestResultMeasureImpl}
     */
    public static synchronized InfluxTestResultMeasureImpl getInstance() {
        if (measure == null) {
            measure = new InfluxTestResultMeasureImpl();
        }
        return measure;
    }

    public void configureMeasureIdempotent() {
        if (!clientConfigured.getAndSet(true)) {
            // Idempotent operation
            InfluxPropertiesProvider propertiesProvider = new InfluxPropertiesProvider();
            influxClient = InfluxClient.buildClient(new InfluxClientConfiguration(propertiesProvider));
            testName = propertiesProvider.provideTestName();
            runId = propertiesProvider.provideRunId();
            hostName = propertiesProvider.provideHostName();
            isClientClosed = new AtomicBoolean(false);
            final var MESSAGE = "Configured Influx Test Result Client";
            LOGGER.info(MESSAGE);
            System.out.println(MESSAGE);
            clientConfigureLatch.countDown();
        } else {
            try {
                // Block other threads until the idempotent operation finish
                // Will return immediately if the influx client is configured.
                clientConfigureLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    /**
     * Make this method Thread safe
     */
    @Override
    public void close() {
        if (!isClientClosed.getAndSet(true)) {
            super.close();
            measure = null;
            final var MESSAGE = "Influx client closed, Measure singleton reset";
            LOGGER.info(MESSAGE);
            System.out.println(MESSAGE);
        }
    }
}
