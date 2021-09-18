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

public class TestResultMeasureManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResultMeasureManager.class);

    private static TestResultMeasureManager INSTANCE;

    private InfluxTestResultMeasureImpl testResultMeasure;

    private AtomicBoolean beingConfigured;

    private CountDownLatch configureLatch;

    private boolean measureSubResult;

    private TestResultMeasureManager() {
    }

    /**
     * Make a new singleton instance and reset to original state
     *
     * @return the {@link TestResultMeasureManager}
     */
    public static synchronized TestResultMeasureManager makeInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestResultMeasureManager();
        }
        INSTANCE.beingConfigured = new AtomicBoolean(false);
        INSTANCE.configureLatch = new CountDownLatch(1);
        INSTANCE.measureSubResult = false;
        return INSTANCE;
    }

    /**
     * Get the Influx Measure for Test Result measure. This method initializes an
     * {@link InfluxClient} while blocking other threads until the initialization
     * is done, return immediately if the initialization is already executed.
     *
     * @return an implementation of {@link InfluxTestResultMeasure} or null if the
     * {@link InfluxClient} cannot be initialized.
     */
    public InfluxTestResultMeasure getInfluxMeasure() {
        try {
            initializeInfluxMeasureBlock();
        } catch (PluginException | InterruptedException e) {
            LOGGER.error("Could not create {}. Reason: {}",
                    InfluxTestResultMeasure.class.getSimpleName() , e.getMessage());
            Thread.currentThread().interrupt();
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
                measureSubResult = propertiesProvider.measureSubResult();
                testResultMeasure = new InfluxTestResultMeasureImpl(influxClient, propertiesProvider);
                testResultMeasure.setSaveErrorResponse(propertiesProvider.provideSaveErrorResponseOption());
            } finally {
                configureLatch.countDown();
            }
        } else {
            // Block other threads until the influx client is configured
            configureLatch.await();
        }
    }

    /**
     * Return the Property Configuration passed when execute test
     *
     * @return true if measure Sub Result
     */
    public boolean measureSubResult() {
        return measureSubResult;
    }
}
