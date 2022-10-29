package com.nttdatavds.measures;

import com.nttdatavds.influxdb.InfluxClient;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMeasureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMeasureManager.class.getSimpleName());

    private final InfluxClient influxClient;

    private final TestResultMeasure testResultMeasure;

    private final TestStateMeasure testStateMeasure;

    private ScheduledExecutorService scheduler;

    public TestMeasureManager(InfluxClient influxClient, TestResultMeasure testResultMeasure, TestStateMeasure testStateMeasure) {
        this.influxClient = influxClient;
        this.testResultMeasure = testResultMeasure;
        this.testStateMeasure = testStateMeasure;
    }

    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
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
        scheduler.scheduleAtFixedRate(() -> {
            testStateMeasure.writeUserMetric(userMetric);
        }, 1, 100, TimeUnit.MILLISECONDS);
    }

    public void closeManager() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        try {
            boolean terminated = scheduler.awaitTermination(30, TimeUnit.SECONDS);
            if (terminated) {
                LOGGER.info("influxDB scheduler terminated!");
            }
        } catch (InterruptedException e) {
            LOGGER.error("Error waiting for end of scheduler", e);
            Thread.currentThread().interrupt();
        }
        influxClient.closeClient();
        LOGGER.info("Close InfluxClient @ {}", influxClient.getUrl());
    }

}
