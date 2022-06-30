package org.ifisolution.measures;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.ifisolution.configuration.MeasureSettings;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.measures.impl.TestResultMeasureImpl;
import org.ifisolution.measures.impl.TestStateMeasureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMeasureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMeasureManager.class.getSimpleName());

    private InfluxClient influxClient;

    private TestResultMeasure testResultMeasure;

    private TestStateMeasure testStateMeasure;

    private TestMeasureManager() {
    }

    public static TestMeasureManager createManager(InfluxClient influxClient, MeasureSettings measureSettings) {
        TestMeasureManager INSTANCE = new TestMeasureManager();
        INSTANCE.influxClient = influxClient;
        INSTANCE.testResultMeasure = new TestResultMeasureImpl(influxClient, measureSettings);
        INSTANCE.testStateMeasure = new TestStateMeasureImpl(influxClient, measureSettings);
        return INSTANCE;
    }

    public void writeTestStarted() {
        testStateMeasure.writeStartState();
    }

    public void writeTestEnded() {
        testStateMeasure.writeFinishState();
    }

    public void writeTestResult(SampleResult sampleResult) {
        testResultMeasure.writeTestResult(sampleResult);
    }

    public void writeUserMetric(UserMetric userMetric) {
        testStateMeasure.writeUserMetric(userMetric);
    }

    public void closeInfluxClient() {
        influxClient.closeClient();
        LOGGER.info("Close InfluxClient @ {}", influxClient.getUrl());
    }

}
