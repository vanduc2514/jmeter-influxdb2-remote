package org.ifisolution.plugins.listeners;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientException;
import org.ifisolution.measures.InfluxTestStateMeasure;
import org.ifisolution.measures.TestResultMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InfluxTestStateListener extends AbstractBackendListenerClient {

    public static final int SCHEDULER_THREAD_POOL_SIZE = 1;
    public static final int VIRTUAL_USER_INTERVAL = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxTestStateListener.class);

    private InfluxTestStateMeasure testStateMeasure;

    private TestResultMeasure testResultMeasure;

    private InfluxClient influxClient;

    private boolean measureSubResult;

    private ScheduledExecutorService scheduler;

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        List<SampleResult> allResults = new ArrayList<>();

        for (SampleResult res : sampleResults) {
            allResults.add(res);
            if (measureSubResult) {
                Collections.addAll(allResults, res.getSubResults());
            }
        }
        for (SampleResult res : allResults) {
            getUserMetrics().add(res);
            if (testResultMeasure != null) {
                testResultMeasure.writeTestResult(res);
            }
        }
    }

    @Override
    public void setupTest(BackendListenerContext context) {
        try {
            boostrapMeasure();
            scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL_SIZE);
            testStateMeasure.writeStartState();
            // Constantly write virtual user with an interval of 5 seconds
            scheduler.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            testStateMeasure.writeUserMetric(getUserMetrics());
                        }
                    },
                    1, VIRTUAL_USER_INTERVAL, TimeUnit.SECONDS
            );
        } catch (InfluxClientException e) {
            LOGGER.error("Could not create {}. Reason: {}",
                    InfluxTestStateMeasure.class.getSimpleName(), e.getMessage());
        }
    }

    private void boostrapMeasure() throws InfluxClientException {
//        ClientProperties clientProperties = new ClientProperties();
//        boolean standaloneMode = clientProperties.isStandalone();
//        influxClient = InfluxClient.buildClient(new InfluxClientBuilder(clientProperties));
//        LOGGER.info("Acquired Influx Client to {}", influxClient.getUrl());
//        testStateMeasure = new InfluxTestStateMeasureImpl(influxClient, clientProperties);
//        if (standaloneMode) {
//            LOGGER.info("Using Standalone Mode with master. In this mode, " +
//                    "the test state and test result metric will be sent using only master machine");
//            testResultMeasure = new TestResultMeasureImpl(influxClient, clientProperties);
//            measureSubResult = clientProperties.measureSubResult();
//        }
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        if (testStateMeasure == null || scheduler == null) {
            return;
        }
        scheduler.shutdown();
        testStateMeasure.writeFinishState();

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
        super.teardownTest(context);
    }
}
