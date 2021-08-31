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

public class InfluxSampleSender extends BatchSampleSender implements Serializable {

    private static final long serialVersionUID = 3371144997364645511L;

    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxSampleSender.class);

    private InfluxTestResultMeasure defaultResultMeasure;

    private boolean measureConfigured;

    /**
     * This constructor is invoked through reflection found in {@link SampleSenderFactory}
     */
    public InfluxSampleSender(RemoteSampleListener listener) {
        super(listener);
    }

    @Override
    public void testEnded(String host) {
        if (defaultResultMeasure != null) {
            defaultResultMeasure.close();
            LOGGER.info("Influx Connection closed");
        }
        super.testEnded(host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        //Make sure the Influxdb measure is created only once when first sample event occurred
        if (!measureConfigured) {
            defaultResultMeasure = InfluxTestResultMeasureImpl.getInstance();
            measureConfigured = true;
        }
        defaultResultMeasure.writeTestResult(e);
        LOGGER.info("Wrote Test to Influx");
        super.sampleOccurred(e);
    }

    /**
     * Internal method called by RMI. This method is invoked after this object is created
     */
    private Object readResolve() throws ObjectStreamException {
        //Initialize a new measure
        LOGGER.info("Use server configured for this run: " + isClientConfigured());
        return this;
    }
}
