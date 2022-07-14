/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ifisolution;

import org.apache.jmeter.samplers.BatchSampleSender;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleSenderFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.ifisolution.configuration.ClientProperties;
import org.ifisolution.configuration.MeasureSettings;
import org.ifisolution.influxdb.InfluxClient;
import org.ifisolution.influxdb.InfluxClientException;
import org.ifisolution.measures.TestMeasureManager;
import org.ifisolution.measures.TestResultMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InfluxSampleSender extends BatchSampleSender {

    private static final long serialVersionUID = 3371144997364645511L;

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxSampleSender.class.getSimpleName());

    // fields that are initialized from the client instance
    // then is copied to the server instance by RMI
    private String influxConnectionUrl;

    private String influxToken;

    private String influxOrganizationName;

    private String influxBucketName;

    private String testName;

    private String testRunId;

    private boolean measureSubResult;

    private boolean saveErrorResponse;

    // field that is initialized from the server instance
    private transient TestMeasureManager measureManager;

    public static final int SCHEDULER_THREAD_POOL_SIZE = 1;

    public static final int VIRTUAL_USER_INTERVAL_MILLI = 1;

    private transient ScheduledExecutorService scheduler;

    private transient UserMetric userMetric;

    private static final Object LOCK = new Object();

    /**
     * This constructor is invoked through reflection found in {@link SampleSenderFactory}
     *
     * <b>This constructor is called by client instance</b>
     */
    public InfluxSampleSender(RemoteSampleListener listener) {
        super(listener);
        if (isClientConfigured()) {
            LOGGER.info("Using InfluxSampleSender (client settings) for this run. Settings are initialized from client instance");
            initializedFields();
        } else {
            LOGGER.info("Using InfluxSampleSender (server settings) for this run");
        }
        logFields();
    }

    @Override
    public void testEnded(String host) {
        if (measureManager != null && scheduler != null) {
            measureManager.writeTestEnded();
            synchronized (LOCK) {
                while (true) {
                    if (userMetric.getFinishedThreads() == userMetric.getStartedThreads()) {
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
                        measureManager.closeInfluxClient();
                        break;
                    }
                }
            }
        }
        super.testEnded(host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        if (measureManager != null) {
            measureManager.writeTestResult(e.getResult());
        } else {
            LOGGER.warn("No {} is configured. This remote machine does not send Test Result Point to Influx",
                    TestResultMeasure.class.getSimpleName());
        }
        super.sampleOccurred(e);
    }

    /**
     * Processed by the RMI server code; acts as testStarted().
     *
     * @return this
     * @throws ObjectStreamException never
     */
    private Object readResolve() throws ObjectStreamException {
        String hostName = JMeterUtils.getLocalHostName();
        LOGGER.info("Configure InfluxSampleSender @ {}", hostName);
        if (isClientConfigured()) {
            LOGGER.info("Using InfluxSampleSender (client settings) for this run. Settings are copied from client instance");
        } else {
            LOGGER.info("Using InfluxSampleSender (server settings) for this run. Settings are initialized from server instance");
            initializedFields();
        }
        logFields();

        synchronized (LOCK) {
            if (measureManager == null) {
                try {
                    bootstrapMeasureManager(hostName);
                    scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL_SIZE);
                    measureManager.writeTestStarted();
                    userMetric = new UserMetric();
                    scheduler.scheduleAtFixedRate(
                            () -> {
                                measureManager.writeUserMetric(userMetric);
                                LOGGER.info("Writing user metric");
                            },
                            1, VIRTUAL_USER_INTERVAL_MILLI, TimeUnit.MILLISECONDS
                    );
                } catch (InfluxClientException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        return this;
    }

    private void bootstrapMeasureManager(String hostName) throws InfluxClientException {
        MeasureSettings measureSettings = MeasureSettings.builder()
                .hostName(hostName)
                .testName(testName)
                .testRunId(testRunId)
                .measureSubResult(measureSubResult)
                .saveErrorResponse(saveErrorResponse)
                .build();
        InfluxClient influxClient = InfluxClient.builder()
                .connectionUrl(influxConnectionUrl)
                .token(influxToken)
                .organization(influxOrganizationName)
                .bucket(influxBucketName)
                .build();
        measureManager = TestMeasureManager.createManager(influxClient, measureSettings);
    }

    private void initializedFields() {
        ClientProperties clientProperties = new ClientProperties();
        influxConnectionUrl = clientProperties.InfluxConnectionUrl();
        influxToken = clientProperties.influxToken();
        influxOrganizationName = clientProperties.influxOrganizationName();
        influxBucketName = clientProperties.influxBucketName();
        testName = clientProperties.testName();
        testRunId = clientProperties.testRunId();
        measureSubResult = clientProperties.measureSubResult();
        saveErrorResponse = clientProperties.saveErrorResponse();
    }

    private void logFields() {
        LOGGER.info("InfluxDB: url={}, organization={}, bucket={}",
                influxConnectionUrl, influxOrganizationName, influxBucketName);
        LOGGER.info("Test: name={}, runId={}, measureSubResult={}, saveErrorResponse={}",
                testName, testRunId, measureSubResult, saveErrorResponse);
    }

}
