package org.ifisolution.measures;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.ifisolution.configuration.MeasureSettings;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.measures.impl.TestResultMeasureImpl;
import org.ifisolution.measures.impl.TestStateMeasureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMeasureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestMeasureManager.class.getSimpleName());

    private static TestMeasureManager INSTANCE;

    private InfluxClient influxClient;

    private TestResultMeasure testResultMeasure;

    private TestStateMeasure testStateMeasure;

    private ScheduledExecutorService scheduler;

    private TestMeasureManager() {
    }

    public synchronized static TestMeasureManager getManagerInstance(InfluxClient influxClient, MeasureSettings measureSettings) {
        if (INSTANCE == null) {
            INSTANCE = new TestMeasureManager();
            INSTANCE.influxClient = influxClient;
            INSTANCE.testResultMeasure = new TestResultMeasureImpl(influxClient, measureSettings);
            INSTANCE.testStateMeasure = new TestStateMeasureImpl(influxClient, measureSettings);
        }
        if (INSTANCE.scheduler == null || INSTANCE.scheduler.isTerminated()) {
            // TODO: Extract pool size to parameter
            INSTANCE.scheduler = Executors.newScheduledThreadPool(1);
        }
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
