package org.ifisolution.plugins.listeners;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.exeptions.PluginException;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.InfluxTestStateMeasure;
import org.ifisolution.measures.impl.InfluxTestResultMeasureImpl;
import org.ifisolution.measures.impl.InfluxTestStateMeasureImpl;
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

    private InfluxTestResultMeasure testResultMeasure;

    private InfluxClient influxClient;

    private boolean measureSubResult;

    private ScheduledExecutorService scheduler;

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        List<SampleResult> allResults = new ArrayList<>();
        sampleResults.forEach(res -> {
            allResults.add(res);
            if (measureSubResult) {
                Collections.addAll(allResults, res.getSubResults());
            }
        });

        allResults.forEach(res -> {
            getUserMetrics().add(res);
            if (testStateMeasure != null) {
                testResultMeasure.writeTestResult(res);
            }
        });

    }

    @Override
    public void setupTest(BackendListenerContext context) {
        try {
            boostrapMeasure();
            scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL_SIZE);
            testStateMeasure.writeStartState();
            // Constantly write virtual user with an interval of 5 seconds
            scheduler.scheduleAtFixedRate(
                    () -> testStateMeasure.writeUserMetric(getUserMetrics()),
                    1, VIRTUAL_USER_INTERVAL, TimeUnit.SECONDS
            );
        } catch (PluginException e) {
            LOGGER.error("Could not create {}. Reason: {}",
                    InfluxTestStateMeasure.class.getSimpleName(), e.getMessage());
        }
    }

    private void boostrapMeasure() throws PluginException {
        InfluxPropertiesProvider propertiesProvider = new InfluxPropertiesProvider();
        boolean standaloneMode = propertiesProvider.isStandalone();
        influxClient = InfluxClient.buildClient(new InfluxClientConfiguration(propertiesProvider));
        LOGGER.info("Acquired Influx Client to {}", influxClient.getHostName());
        testStateMeasure = new InfluxTestStateMeasureImpl(influxClient, propertiesProvider);
        if (standaloneMode) {
            testResultMeasure = new InfluxTestResultMeasureImpl(influxClient, propertiesProvider);
            measureSubResult = propertiesProvider.measureSubResult();
        }
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
