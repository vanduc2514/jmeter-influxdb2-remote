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

package com.nttdatavds;

import com.nttdatavds.configuration.PluginConfiguration;
import com.nttdatavds.influxdb.InfluxClient;
import com.nttdatavds.influxdb.InfluxClientException;
import com.nttdatavds.measures.Measures;
import com.nttdatavds.measures.TestResultMeasure;
import com.nttdatavds.measures.TestStateMeasure;
import org.apache.jmeter.samplers.BatchSampleSender;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleSenderFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InfluxSampleSender extends BatchSampleSender {

    private static final long serialVersionUID = 3371144997364645511L;

    private static final String CLASS_NAME = InfluxSampleSender.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(CLASS_NAME);

    private String influxConnectionUrl;

    private String influxToken;

    private String influxOrganizationName;

    private String influxBucketName;

    private String testName;

    private String testRunId;

    private boolean measureSubResult;

    private boolean saveErrorResponse;

    private int userMetricPoolSize;

    private int userMetricInterval;

    private int writeBatchSize;

    private int writeFlushInterval;

    private int writeBufferLimit;

    // field that is initialized from the slave instance
    private transient ScheduledExecutorService scheduler;

    private transient TestResultMeasure testResultMeasure;

    private transient TestStateMeasure testStateMeasure;

    private transient InfluxClient influxClient;

    /**
     * Dynamic constructor which is invoked by both master and slave instance through
     * reflection found in {@link SampleSenderFactory}. If configurations are sent
     * from mater, this Sender will be configured from there and then sent to the slave
     * along with configured properties. Otherwise, its properties are configured in the
     * {@link #readResolve()} magic method.
     */
    public InfluxSampleSender(RemoteSampleListener listener) {
        super(listener);
        if (isClientConfigured()) {
            configurePlugin();
            LOGGER.info("Use {} (master configurations) for this run", CLASS_NAME);
        } else {
            LOGGER.info("Use {} (slave configurations) for this run", CLASS_NAME);
        }
        logFields();
    }

    @Override
    public void testEnded(String host) {
        if (testStateMeasure != null) {
            testStateMeasure.writeFinishState();
            LOGGER.debug("Sent Test End to Influx");
        }
        scheduler.shutdown();
        LOGGER.debug("Scheduler for User Metric Shut down");
        influxClient.closeClient();
        LOGGER.debug("Influx Client closed!");
        super.testEnded(host);
        LOGGER.debug("Sent Test End to Master");
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        if (testResultMeasure != null) {
            testResultMeasure.writeTestResult(e.getResult());
            LOGGER.debug("Sent Test Result to Influx");
        } else {
            LOGGER.warn("No Influx Configuration found. Cannot send measure to Influx!");
        }
        super.sampleOccurred(e);
        LOGGER.debug("Sent Test Result to Master.");
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
            LOGGER.info("Use {} (master configurations) for this run", CLASS_NAME);
        } else {
            configurePlugin();
            LOGGER.info("Use {} (slave configurations) for this run", CLASS_NAME);
        }
        logFields();

        try {
            influxClient = InfluxClient.builder()
                    .connectionUrl(influxConnectionUrl)
                    .token(influxToken)
                    .organization(influxOrganizationName)
                    .bucket(influxBucketName)
                    .writeBatchSize(writeBatchSize)
                    .writeFlushInterval(writeFlushInterval)
                    .writeBufferLimit(writeBufferLimit)
                    .build();
        } catch (InfluxClientException clientException) {
            throw new InvalidObjectException(clientException.getMessage());
        }

        testResultMeasure = Measures.testResultMeasureBuilder(influxClient)
                .hostName(hostName)
                .testName(testName)
                .testRunId(testRunId)
                .measureSubResult(measureSubResult)
                .saveErrorResponse(saveErrorResponse)
                .build();
        testStateMeasure = Measures.testStateMeasureBuilder(influxClient)
                .hostName(hostName)
                .testName(testName)
                .testRunId(testRunId)
                .build();
        scheduler = Executors.newScheduledThreadPool(userMetricPoolSize);

        testStateMeasure.writeStartState();
        LOGGER.debug("Sent Test Start to Influx");
        scheduler.scheduleAtFixedRate(() -> {
            testStateMeasure.writeUserMetric(new UserMetric());
            LOGGER.debug("Sent User Metric to Influx");
        }, 1, userMetricInterval, TimeUnit.SECONDS);

        return this;
    }

    private void configurePlugin() {
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        // Test Run properties
        influxConnectionUrl = pluginConfiguration.InfluxConnectionUrl();
        influxToken = pluginConfiguration.influxToken();
        influxOrganizationName = pluginConfiguration.influxOrganizationName();
        influxBucketName = pluginConfiguration.influxBucketName();
        testName = pluginConfiguration.testName();
        testRunId = pluginConfiguration.testRunId();
        measureSubResult = pluginConfiguration.measureSubResult();
        saveErrorResponse = pluginConfiguration.saveErrorResponse();
        // Tuning properties
        userMetricInterval = pluginConfiguration.userMetricInterval();
        userMetricPoolSize = pluginConfiguration.userMetricPoolSize();
        writeBatchSize = pluginConfiguration.writeBatchSize();
        writeFlushInterval = pluginConfiguration.writeFlushInterval();
        writeBufferLimit = pluginConfiguration.writeBufferLimit();
    }

    private void logFields() {
        LOGGER.info("InfluxDB: url={}, organization={}, bucket={}",
                influxConnectionUrl, influxOrganizationName, influxBucketName);
        LOGGER.info("Test: name={}, runId={}, measureSubResult={}, saveErrorResponse={}",
                testName, testRunId, measureSubResult, saveErrorResponse);
    }

}
