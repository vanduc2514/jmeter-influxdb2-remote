package org.ifisolution.plugins.listeners;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.exeptions.PluginException;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.ifisolution.measures.InfluxTestStateMeasure;
import org.ifisolution.measures.impl.InfluxTestStateMeasureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InfluxTestStateListener extends AbstractBackendListenerClient {

    public static final int SCHEDULER_THREAD_POOL_SIZE = 1;
    public static final int VIRTUAL_USER_INTERVAL = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxTestStateListener.class);

    private InfluxTestStateMeasure testStateMeasure;

    private ScheduledExecutorService scheduler;

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        //NOOP since this was handled in the SampleSender
    }

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        testStateMeasure = createMeasure();
        scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL_SIZE);
        testStateMeasure.writeStartState();
        scheduler.scheduleAtFixedRate(
                () -> testStateMeasure.writeUserMetric(getUserMetrics()),
                1, VIRTUAL_USER_INTERVAL, TimeUnit.SECONDS
        );
    }

    private InfluxTestStateMeasure createMeasure() throws PluginException {
        InfluxPropertiesProvider propertiesProvider = new InfluxPropertiesProvider();
        InfluxClient influxClient = InfluxClient.buildClient(
                new InfluxClientConfiguration(propertiesProvider)
        );
        LOGGER.info("Acquired Influx Client to {}", influxClient.getHostName());
        return new InfluxTestStateMeasureImpl(influxClient, propertiesProvider);
    }

    @Override
    public void teardownTest(BackendListenerContext context) {
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
            LOGGER.error("Error waiting for end of scheduler " + e);
        }

        testStateMeasure.closeInfluxConnection();
    }
}
