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

package com.github.vanduc2514;

import com.github.vanduc2514.configuration.PluginConfiguration;
import com.github.vanduc2514.influxdb.InfluxClientException;
import com.github.vanduc2514.influxdb.InfluxClientProxy;
import com.github.vanduc2514.measures.Measures;
import com.github.vanduc2514.measures.TestResultMeasure;
import com.github.vanduc2514.measures.TestStateMeasure;
import org.apache.jmeter.samplers.BatchSampleSender;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleSenderFactory;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link org.apache.jmeter.samplers.SampleSender} which send the Test Result
 * contains Sample Data and Test State contains Virtual User metric and test start,
 * test finish.
 */
public class InfluxSampleSender extends BatchSampleSender {

    private static final long serialVersionUID = 3371144997364645511L;

    private static final String CLASS_NAME = InfluxSampleSender.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(CLASS_NAME);

    private static final long TERMINATE_TIMEOUT = 60;

    private String influxConnectionUrl;

    private String influxOrganizationName;

    private String influxBucketName;

    private char[] influxToken;

    private String testName;

    private String testRunId;

    private boolean measureSubResult;

    private boolean saveErrorResponse;

    private List<String> excludedThreadGroups;

    private int userMetricPoolSize;

    private int userMetricInterval;

    private int writeBatchSize;

    private int writeFlushInterval;

    private int writeBufferLimit;

    // fields that are initialized from the slave instance
    private transient ScheduledExecutorService scheduler;

    private transient TestResultMeasure testResultMeasure;

    private transient TestStateMeasure testStateMeasure;

    private transient InfluxClientProxy influxClientProxy;

    /**
     * Dynamic constructor which is invoked by both master and slave instance through
     * reflection found in {@link SampleSenderFactory}. If configurations are sent
     * from mater, this Sender will be configured from there and then sent to the slave
     * along with configured properties. Otherwise, its properties are configured in the
     * {@code readResolve()} magic method.
     */
    public InfluxSampleSender(RemoteSampleListener listener) {
        super(listener);
        if (isClientConfigured()) {
            configurePlugin();
            LOGGER.info("Use master configuration for this run");
        } else {
            LOGGER.info("Use slave configuration for this run");
        }
        String configuration = this.toString();
        LOGGER.info(configuration);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        // Ignore SetupThreadGroup and PostThreadGroup
        String threadGroupName = JMeterContextService.getContext().getThreadGroup().getName();
        if (!inExcludedGroups(threadGroupName)) {
            testResultMeasure.writeTestResult(e.getResult());
            LOGGER.debug("Sent Test Result to Influx");
            super.sampleOccurred(e);
            LOGGER.debug("Sent Test Result to Master.");
        }
    }

    @Override
    public void testEnded(String host) {
        if (testStateMeasure != null) {
            testStateMeasure.writeFinishState();
            LOGGER.debug("Sent Test End to Influx");
        }
        try {
            LOGGER.info("Gracefully Terminate Scheduler. Timeout: {} seconds", TERMINATE_TIMEOUT);
            scheduler.shutdown();
            if (scheduler.awaitTermination(TERMINATE_TIMEOUT, TimeUnit.SECONDS)) {
                influxClientProxy.closeClient();
                LOGGER.info("Influx Client closed!");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Scheduler cannot be terminated!. Error: {}", e.getMessage());
        } catch (InfluxClientException e) {
            LOGGER.warn("Influx Client cannot be closed!. Error: {}", e.getMessage());
        }
        super.testEnded(host);
        LOGGER.debug("Sent Test End to Master");
    }

    /**
     * Processed by the RMI server code; acts as testStarted().
     */
    private Object readResolve() throws ObjectStreamException {
        String hostName = JMeterUtils.getLocalHostName();
        if (isClientConfigured()) {
            LOGGER.info("Use master configuration for this run");
        } else {
            configurePlugin();
            LOGGER.info("Use slave configuration for this run");
        }
        String configuration = this.toString();
        LOGGER.info(configuration);

        try {
            influxClientProxy = InfluxClientProxy.make(
                    influxConnectionUrl,
                    influxToken,
                    influxOrganizationName,
                    influxBucketName,
                    writeBatchSize,
                    writeFlushInterval,
                    writeBufferLimit
            );
        } catch (InfluxClientException clientException) {
            throw new InvalidObjectException(clientException.getMessage());
        }

        testResultMeasure = Measures.testResultMeasureBuilder(influxClientProxy)
                .hostName(hostName)
                .testName(testName)
                .testRunId(testRunId)
                .measureSubResult(measureSubResult)
                .saveErrorResponse(saveErrorResponse)
                .build();
        testStateMeasure = Measures.testStateMeasureBuilder(influxClientProxy)
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

    /**
     * Invoked only once. If properties are sent from master, then this
     * method is invoked from there. Otherwise, it will be invoked by slave.
     */
    private void configurePlugin() {
        // Test Run properties
        influxConnectionUrl = PluginConfiguration.influxConnectionUrl();
        influxOrganizationName = PluginConfiguration.influxOrganizationName();
        influxBucketName = PluginConfiguration.influxBucketName();
        influxToken = PluginConfiguration.influxToken();
        testName = PluginConfiguration.testName();
        testRunId = PluginConfiguration.testRunId();
        measureSubResult = PluginConfiguration.measureSubResult();
        saveErrorResponse = PluginConfiguration.saveErrorResponse();
        // Tuning properties
        userMetricInterval = PluginConfiguration.userMetricInterval();
        userMetricPoolSize = PluginConfiguration.userMetricPoolSize();
        writeBatchSize = PluginConfiguration.writeBatchSize();
        writeFlushInterval = PluginConfiguration.writeFlushInterval();
        writeBufferLimit = PluginConfiguration.writeBufferLimit();
        excludedThreadGroups = PluginConfiguration.excludedThreadGroups();
    }

    /**
     * @return {@code true} if the current thread in excluded group, which
     * this plugin does not send the result to InfluxDB
     */
    private boolean inExcludedGroups(String threadGroupName) {
        return excludedThreadGroups.contains(threadGroupName);
    }

    // Log Configuration as JSON
    @Override
    public String toString() {
        return "{"
                + "\"masterConfiguration\":\"" + isClientConfigured() + "\""
                + ", \"influxConnectionUrl\":\"" + influxConnectionUrl + "\""
                + ", \"influxOrganizationName\":\"" + influxOrganizationName + "\""
                + ", \"influxBucketName\":\"" + influxBucketName + "\""
                + ", \"testName\":\"" + testName + "\""
                + ", \"testRunId\":\"" + testRunId + "\""
                + ", \"measureSubResult\":\"" + measureSubResult + "\""
                + ", \"saveErrorResponse\":\"" + saveErrorResponse + "\""
                + ", \"userMetricPoolSize\":\"" + userMetricPoolSize + "\""
                + ", \"userMetricInterval\":\"" + userMetricInterval + "\""
                + ", \"writeBatchSize\":\"" + writeBatchSize + "\""
                + ", \"writeFlushInterval\":\"" + writeFlushInterval + "\""
                + ", \"writeBufferLimit\":\"" + writeBufferLimit + "\""
                + "}";
    }
}
