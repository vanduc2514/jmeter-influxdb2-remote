package org.ifisolution.measures.impl;

import org.ifisolution.configuration.InfluxPropertiesProvider;
import org.ifisolution.exeptions.PluginException;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientConfiguration;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestResultManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResultManager.class);

    private static TestResultManager INSTANCE;

    private InfluxTestResultMeasureImpl testResultMeasure;

    private AtomicBoolean beingConfigured;

    private CountDownLatch configureLatch;

    private TestResultManager() {
    }

    /**
     * Make a new singleton instance and reset to original state
     *
     * @return the {@link TestResultManager}
     */
    public static synchronized TestResultManager makeInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestResultManager();
        }
        INSTANCE.beingConfigured = new AtomicBoolean(false);
        INSTANCE.configureLatch = new CountDownLatch(1);
        return INSTANCE;
    }

    /**
     * Get the Influx Measure for Test Result measure. This method initializes an
     * {@link InfluxClient} while blocking other threads until the initialization
     * is done, will return immediately if the initialization is already executed.
     *
     * @return an implementation of {@link InfluxTestResultMeasure} or null if the
     * {@link InfluxClient} cannot be initialized.
     */
    public InfluxTestResultMeasure getInfluxMeasure() {
        try {
            initializeInfluxMeasureBlock();
        } catch (PluginException | InterruptedException e) {
            LOGGER.error("Could not create influx measure. Reason: {}", e.getMessage());
        }
        return testResultMeasure;
    }

    private void initializeInfluxMeasureBlock() throws PluginException, InterruptedException {
        if (!beingConfigured.getAndSet(true)) {
            // Runs only once
            InfluxPropertiesProvider propertiesProvider = new InfluxPropertiesProvider();
            try {
                InfluxClient influxClient = InfluxClient.buildClient(
                        new InfluxClientConfiguration(propertiesProvider)
                );
                LOGGER.info("Acquired Influx Client to {}", influxClient.getHostName());
                testResultMeasure = new InfluxTestResultMeasureImpl(influxClient, propertiesProvider);
            } finally {
                configureLatch.countDown();
            }
        } else {
            // Block other threads until the influx client is configured
            configureLatch.await();
        }
    }

}
