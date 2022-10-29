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

import com.nttdatavds.configuration.MeasureSettings;
import com.nttdatavds.configuration.PluginConfiguration;
import com.nttdatavds.influxdb.InfluxClient;
import com.nttdatavds.influxdb.InfluxClientException;
import com.nttdatavds.measures.TestMeasureManager;
import org.apache.jmeter.samplers.BatchSampleSender;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleSenderFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.UserMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;

public class InfluxSampleSender extends BatchSampleSender {

    private static final long serialVersionUID = 3371144997364645511L;

    private static final String CLASS_NAME = InfluxSampleSender.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(CLASS_NAME);

    // fields that are initialized from the master instance
    // then is sent to the slave instance through RMI
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

    // field that is initialized from the slave instance
    private transient TestMeasureManager measureManager;

    /**
     * This constructor is invoked through reflection found in {@link SampleSenderFactory}
     *
     * <b>This constructor is called by slave instance</b>
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
        if (measureManager != null) {
            measureManager.writeTestEnded();
            measureManager.closeManager();
        }
        super.testEnded(host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        if (measureManager != null) {
            measureManager.writeTestResult(e.getResult());
            LOGGER.info("Sent Test Result to Influx");
        } else {
            LOGGER.warn("No Influx Configuration found. Cannot send measure to Influx!");
        }
        super.sampleOccurred(e);
        LOGGER.info("Sent Test Result to master.");
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

        if (measureManager == null) {
            try {
                bootstrapMeasureManager(hostName);
                measureManager.writeTestStarted();
                measureManager.writeUserMetric(new UserMetric());
            } catch (InfluxClientException e) {
                LOGGER.error(e.getMessage());
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
        measureManager = TestMeasureManager.getManagerInstance(influxClient, measureSettings);
    }

    private void configurePlugin() {
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        influxConnectionUrl = pluginConfiguration.InfluxConnectionUrl();
        influxToken = pluginConfiguration.influxToken();
        influxOrganizationName = pluginConfiguration.influxOrganizationName();
        influxBucketName = pluginConfiguration.influxBucketName();
        testName = pluginConfiguration.testName();
        testRunId = pluginConfiguration.testRunId();
        measureSubResult = pluginConfiguration.measureSubResult();
        saveErrorResponse = pluginConfiguration.saveErrorResponse();
    }

    private void logFields() {
        LOGGER.info("InfluxDB: url={}, organization={}, bucket={}",
                influxConnectionUrl, influxOrganizationName, influxBucketName);
        LOGGER.info("Test: name={}, runId={}, measureSubResult={}, saveErrorResponse={}",
                testName, testRunId, measureSubResult, saveErrorResponse);
    }

}
