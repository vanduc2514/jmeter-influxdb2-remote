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

package org.ifisolution.plugins.samplers;

import org.apache.jmeter.samplers.BatchSampleSender;
import org.apache.jmeter.samplers.RemoteSampleListener;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleSenderFactory;
import org.ifisolution.measures.InfluxTestResultMeasure;
import org.ifisolution.measures.impl.InfluxTestResultMeasureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class InfluxSampleSender extends BatchSampleSender implements Serializable {

    private static final long serialVersionUID = 3371144997364645511L;

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxSampleSender.class);

    private InfluxTestResultMeasure influxResultMeasure;

    //Locking mechanism
    private AtomicBoolean acquireLock;
    private CountDownLatch initCompleteLatch;

    private boolean initialized;

    /**
     * This constructor is invoked through reflection found in {@link SampleSenderFactory}
     */
    public InfluxSampleSender(RemoteSampleListener listener) {
        super(listener);
    }

    @Override
    public void testEnded(String host) {
        if (influxResultMeasure != null) {
            influxResultMeasure.close();
            System.out.println("Test Ended");
            LOGGER.info("Influx Connection closed");
        }
        super.testEnded(host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        // Make sure other threads get block until the initialization of Influx finish
        if (!acquireLock.getAndSet(true)) {
            // Run only once at first invocation
            influxResultMeasure.configureMeasure();
            LOGGER.info("Configured Influx Result Measure");
            initCompleteLatch.countDown();
        } else {
            try {
                initCompleteLatch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        influxResultMeasure.writeTestResult(e);
        super.sampleOccurred(e);
    }

    /**
     * Internal method called by RMI. This method acts as testStart()
     */
    private Object readResolve() throws ObjectStreamException {
        // Initialize a new measure and lock
        // Since this method is called by RMI, other Jmeter configuration is not provided yet
        if (isClientConfigured()) {
            configureSender();
        }
        return this;
    }

    /**
     * Run the initialization once
     */
    private synchronized void configureSender() {
        if (!initialized) {
            LOGGER.info("Use server configuration for this run");
            influxResultMeasure = InfluxTestResultMeasureImpl.getInstance();
            acquireLock = new AtomicBoolean(false);
            initCompleteLatch = new CountDownLatch(1);
            initialized = true;
        }
    }

}
